/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.errors.{routes => errorRoutes}
import forms.EmailPasscodeForm
import models.CurrentProfile
import models.external._
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService.EmailVerified
import services.{ApplicantDetailsService, EmailVerificationService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import views.html.applicant.capture_email_passcode

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureEmailPasscodeController @Inject()(view: capture_email_passcode,
                                               val authConnector: AuthConnector,
                                               val sessionService: SessionService,
                                               emailVerificationService: EmailVerificationService,
                                               applicantDetailsService: ApplicantDetailsService)
                                              (implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        getEmailAddress.map { email =>
          Ok(view(email, EmailPasscodeForm.form, isTransactor = false, isNewPasscode = false))
        }
  }

  val requestNew: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        getEmailAddress.flatMap { email =>
          emailVerificationService.requestEmailVerificationPasscode(email).flatMap {
            case AlreadyVerifiedEmailAddress =>
              applicantDetailsService.saveApplicantDetails(EmailVerified(emailVerified = true)).map { _ =>
                Redirect(routes.EmailAddressVerifiedController.show)
              }
            case RequestEmailPasscodeSuccessful =>
              Future.successful(Ok(view(email, EmailPasscodeForm.form, isTransactor = false, isNewPasscode = true)))
            case MaxEmailsExceeded =>
              Future.successful(Redirect(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show))
          }
        }
  }

  def submit(isNewPasscode: Boolean): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        EmailPasscodeForm.form.bindFromRequest().fold(
          formWithErrors =>
            getEmailAddress.map { email =>
              BadRequest(view(email, formWithErrors, isTransactor = false, isNewPasscode = isNewPasscode))
            },
          emailPasscode =>
            getEmailAddress.flatMap { email =>
              emailVerificationService.verifyEmailVerificationPasscode(email, emailPasscode) flatMap {
                case EmailAlreadyVerified | EmailVerifiedSuccessfully =>
                  applicantDetailsService.saveApplicantDetails(EmailVerified(emailVerified = true)).map { _ =>
                    Redirect(routes.EmailAddressVerifiedController.show)
                  }
                case PasscodeMismatch =>
                  val incorrectPasscodeForm = EmailPasscodeForm.form.withError(
                    key = EmailPasscodeForm.passcodeKey,
                    message = Messages("capture-email-passcode.error.incorrect_passcode")
                  )
                  Future.successful(BadRequest(view(email, incorrectPasscodeForm, isTransactor = false, isNewPasscode = isNewPasscode)))
                case PasscodeNotFound =>
                  Future.successful(Redirect(errorRoutes.EmailPasscodeNotFoundController.show(
                    controllers.applicant.routes.CaptureEmailAddressController.show.url
                  )))
                case MaxAttemptsExceeded =>
                  Future.successful(Redirect(errorRoutes.EmailPasscodesMaxAttemptsExceededController.show))
              }
            }
        )

  }

  private def getEmailAddress(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[String] =
    applicantDetailsService.getApplicantDetails.map {
      _.contact.email match {
        case Some(email) => email
        case None => throw new InternalServerException("Failed to retrieve email address")
      }
    }

}
