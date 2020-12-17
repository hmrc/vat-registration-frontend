/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.EmailPasscodeForm
import javax.inject.Inject
import models.CurrentProfile
import models.external._
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, EmailVerificationService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import views.html.capture_email_passcode

import scala.concurrent.{ExecutionContext, Future}

class CaptureEmailPasscodeController @Inject()(view: capture_email_passcode,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnector,
                                               emailVerificationService: EmailVerificationService,
                                               applicantDetailsService: ApplicantDetailsService
                                              )(implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        getEmailAddress.map { email =>
          Ok(view(email, routes.CaptureEmailPasscodeController.submit(), EmailPasscodeForm.form))
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        EmailPasscodeForm.form.bindFromRequest().fold(
          formWithErrors =>
            getEmailAddress map { email =>
              BadRequest(view(email, routes.CaptureEmailPasscodeController.submit(), formWithErrors))
            },
          emailPasscode =>
            getEmailAddress flatMap { email =>
              emailVerificationService.verifyEmailVerificationPasscode(email, emailPasscode) flatMap {
                case EmailAlreadyVerified | EmailVerifiedSuccessfully =>
                  for {
                    _ <- applicantDetailsService.saveApplicantDetails(EmailAddress(email))
                    _ <- applicantDetailsService.saveApplicantDetails(EmailVerified(emailVerified = true))
                  } yield {
                    Redirect(routes.EmailAddressVerifiedController.show())
                  }
                case PasscodeMismatch =>
                  val incorrectPasscodeForm = EmailPasscodeForm.form.withError(
                    key = EmailPasscodeForm.passcodeKey,
                    message = Messages("capture-email-passcode.error.incorrect_passcode")
                  )

                  Future.successful(
                    BadRequest(view(email, routes.CaptureEmailPasscodeController.submit(), incorrectPasscodeForm))
                  )

                case PasscodeNotFound => Future.successful(NotImplemented)
                //todo This should redirect to an error page
              }
            }
        )

  }

  private def getEmailAddress(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[String] =
    applicantDetailsService.getApplicantDetails.map {
      _.emailAddress match {
        case Some(EmailAddress(email)) => email
        case None => throw new InternalServerException("Failed to retrieve email address")
      }
    }

}
