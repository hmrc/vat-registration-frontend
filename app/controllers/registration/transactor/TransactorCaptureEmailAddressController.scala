/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.transactor

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.TransactorEmailAddressForm

import javax.inject.Inject
import models.external.{AlreadyVerifiedEmailAddress, EmailAddress, EmailVerified, RequestEmailPasscodeSuccessful}
import play.api.mvc.{Action, AnyContent}
import services.TransactorDetailsService.{TransactorEmail, TransactorEmailVerified}
import services.{EmailVerificationService, SessionProfile, SessionService, TransactorDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.capture_email_address

import scala.concurrent.{ExecutionContext, Future}

class TransactorCaptureEmailAddressController @Inject()(view: capture_email_address,
                                                        val authConnector: AuthConnector,
                                                        val sessionService: SessionService,
                                                        transactorDetailsService: TransactorDetailsService,
                                                        emailVerificationService: EmailVerificationService
                                                     )(implicit appConfig: FrontendAppConfig,
                                                       val executionContext: ExecutionContext,
                                                       baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          transactor <- transactorDetailsService.getTransactorDetails
          filledForm = transactor.email.fold(TransactorEmailAddressForm.form)(transactorEmail => TransactorEmailAddressForm.form.fill(transactorEmail))
        } yield
          Ok(view(routes.TransactorCaptureEmailAddressController.submit, filledForm))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        TransactorEmailAddressForm.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(routes.TransactorCaptureEmailAddressController.submit, formWithErrors))),
          email =>
            transactorDetailsService.saveTransactorDetails(TransactorEmail(email)) flatMap { _ =>
              emailVerificationService.requestEmailVerificationPasscode(email) flatMap {
                case AlreadyVerifiedEmailAddress =>
                  transactorDetailsService.saveTransactorDetails(TransactorEmailVerified(true)) map { _ =>
                    Redirect(routes.TransactorEmailAddressVerifiedController.show)
                  }
                case RequestEmailPasscodeSuccessful =>
                  Future.successful(Redirect(routes.TransactorCaptureEmailPasscodeController.show))
              }
            }
        )
  }

}
