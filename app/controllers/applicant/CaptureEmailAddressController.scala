/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.EmailAddressForm
import models.external.{AlreadyVerifiedEmailAddress, MaxEmailsExceeded, RequestEmailPasscodeSuccessful}
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService.{EmailAddress, EmailVerified}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.CaptureEmailAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureEmailAddressController @Inject()(view: CaptureEmailAddress,
                                              val authConnector: AuthConnector,
                                              val sessionService: SessionService,
                                              applicantDetailsService: ApplicantDetailsService,
                                              emailVerificationService: EmailVerificationService,
                                              vatRegistrationService: VatRegistrationService
                                             )(implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.contact.email.fold(EmailAddressForm.form)(EmailAddressForm.form.fill)
          name <- applicantDetailsService.getApplicantNameForTransactorFlow
        } yield Ok(view(routes.CaptureEmailAddressController.submit, filledForm, name))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        EmailAddressForm.form.bindFromRequest().fold(
          formWithErrors =>
            applicantDetailsService.getApplicantNameForTransactorFlow.map { name =>
              BadRequest(view(routes.CaptureEmailAddressController.submit, formWithErrors, name))
            },
          email =>
            for {
              _ <- applicantDetailsService.saveApplicantDetails(EmailAddress(email))
              isTransactor <- vatRegistrationService.isTransactor
              redirect <-
                if (isTransactor) {
                  applicantDetailsService.saveApplicantDetails(EmailVerified(emailVerified = false)).map { _ =>
                    Redirect(routes.CaptureTelephoneNumberController.show)
                  }
                } else {
                  emailVerificationService.requestEmailVerificationPasscode(email).flatMap {
                    case AlreadyVerifiedEmailAddress =>
                      applicantDetailsService.saveApplicantDetails(EmailVerified(emailVerified = true)).map { _ =>
                        Redirect(routes.EmailAddressVerifiedController.show)
                      }
                    case RequestEmailPasscodeSuccessful =>
                      Future.successful(Redirect(routes.CaptureEmailPasscodeController.show))
                    case MaxEmailsExceeded =>
                      Future.successful(Redirect(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show))
                  }
                }
            } yield {
              redirect
            }
        )
  }

}
