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

import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.BaseController
import forms.EmailAddressForm
import javax.inject.Inject
import models.external.{AlreadyVerifiedEmailAddress, EmailAddress, EmailVerified, RequestEmailPasscodeSuccessful}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ApplicantDetailsService, EmailVerificationService, S4LService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.capture_email_address

import scala.concurrent.{ExecutionContext, Future}

class CaptureEmailAddressController @Inject()(view: capture_email_address,
                                              mcc: MessagesControllerComponents,
                                              val authConnector: AuthConnector,
                                              val keystoreConnector: KeystoreConnector,
                                              applicantDetailsService: ApplicantDetailsService,
                                              emailVerificationService: EmailVerificationService
                                             )(implicit val appConfig: FrontendAppConfig,
                                               ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Future.successful(
          Ok(view(
            routes.CaptureEmailAddressController.submit(),
            EmailAddressForm.form
          ))
        )
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        EmailAddressForm.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(routes.CaptureEmailAddressController.submit(), formWithErrors))),
          email =>
            applicantDetailsService.saveApplicantDetails(EmailAddress(email)).flatMap { _ =>
              emailVerificationService.requestEmailVerificationPasscode(email).map {
                case AlreadyVerifiedEmailAddress =>
                  Redirect(routes.EmailAddressVerifiedController.show())
                case RequestEmailPasscodeSuccessful =>
                  Redirect(routes.CaptureEmailPasscodeController.show())
              }
            }
        )
  }

}
