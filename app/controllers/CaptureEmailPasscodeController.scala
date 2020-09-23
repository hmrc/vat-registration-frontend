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

package controllers

import config.FrontendAppConfig
import connectors.KeystoreConnector
import forms.EmailPasscodeForm
import javax.inject.Inject
import models.external.{EmailAddress, EmailAlreadyVerified, EmailVerifiedSuccessfully, PasscodeNotFound}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{EmailVerificationService, S4LService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.capture_email_passcode

import scala.concurrent.{ExecutionContext, Future}

class CaptureEmailPasscodeController @Inject()(view: capture_email_passcode,
                                               mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnector,
                                               s4LService: S4LService,
                                               emailVerificationService: EmailVerificationService
                                              )(implicit val appConfig: FrontendAppConfig,
                                                ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        s4LService.fetchAndGet[EmailAddress].map {
          case Some(EmailAddress(email)) =>
            Ok(view(
              email,
              routes.CaptureEmailPasscodeController.submit(),
              EmailPasscodeForm.form
            ))
          case None =>
            throw new InternalServerException("Failed to retrieve email address from S4L")
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        EmailPasscodeForm.form.bindFromRequest().fold(
          formWithErrors =>
            s4LService.fetchAndGet[EmailAddress].map {
              case Some(EmailAddress(email)) =>
                BadRequest(view(email, routes.CaptureEmailPasscodeController.submit(), formWithErrors))
              case None =>
                throw new InternalServerException("Failed to retrieve email address from S4L")
            },
          emailPasscode =>
            s4LService.fetchAndGet[EmailAddress].flatMap {
              case Some(EmailAddress(email)) =>
                emailVerificationService.verifyEmailVerificationPasscode(email, emailPasscode).map {
                  case EmailAlreadyVerified | EmailVerifiedSuccessfully =>
                    Redirect(routes.EmailAddressVerifiedController.show())
                  case PasscodeNotFound =>
                    val incorrectPasscodeForm = EmailPasscodeForm.form.withError(
                      key = EmailPasscodeForm.passcodeKey,
                      message = Messages("capture-email-passcode.error.incorrect_passcode")
                    )

                    BadRequest(view(email, routes.CaptureEmailPasscodeController.submit(), incorrectPasscodeForm))
                }
              case None =>
                throw new InternalServerException("Failed to retrieve email address from S4L")
            }
        )
  }

}
