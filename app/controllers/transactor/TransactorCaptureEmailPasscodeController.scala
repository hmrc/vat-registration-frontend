/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.transactor

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.errors.{routes => errorRoutes}
import forms.TransactorEmailPasscodeForm
import models.CurrentProfile
import models.error.MissingAnswerException
import models.external._
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Request}
import services.TransactorDetailsService.TransactorEmailVerified
import services.{EmailVerificationService, SessionProfile, SessionService, TransactorDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import views.html.applicant.CaptureEmailPasscode

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactorCaptureEmailPasscodeController @Inject()(view: CaptureEmailPasscode,
                                                         val authConnector: AuthConnector,
                                                         val sessionService: SessionService,
                                                         emailVerificationService: EmailVerificationService,
                                                         transactorDetailsService: TransactorDetailsService)
                                                        (implicit appConfig: FrontendAppConfig,
                                                         val executionContext: ExecutionContext,
                                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        getEmailAddress.map { email =>
          Ok(view(email, TransactorEmailPasscodeForm.form, isTransactor = true, isNewPasscode = false))
        }
  }

  val requestNew: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        getEmailAddress.flatMap { email =>
          emailVerificationService.requestEmailVerificationPasscode(email).flatMap {
            case AlreadyVerifiedEmailAddress =>
              transactorDetailsService.saveTransactorDetails(TransactorEmailVerified(true)).map { _ =>
                Redirect(routes.TransactorEmailAddressVerifiedController.show)
              }
            case RequestEmailPasscodeSuccessful =>
              Future.successful(Ok(view(email, TransactorEmailPasscodeForm.form, isTransactor = true, isNewPasscode = true)))
            case MaxEmailsExceeded =>
              Future.successful(Redirect(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show))
          }
        }
  }

  def submit(isNewPasscode: Boolean): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        TransactorEmailPasscodeForm.form.bindFromRequest().fold(
          formWithErrors =>
            getEmailAddress map { email =>
              BadRequest(view(email, formWithErrors, isTransactor = true, isNewPasscode = isNewPasscode))
            },
          emailPasscode =>
            getEmailAddress flatMap { email =>
              emailVerificationService.verifyEmailVerificationPasscode(email, emailPasscode) flatMap {
                case EmailAlreadyVerified | EmailVerifiedSuccessfully =>
                  for {
                    _ <- transactorDetailsService.saveTransactorDetails(TransactorEmailVerified(true))
                  } yield {
                    Redirect(routes.TransactorEmailAddressVerifiedController.show)
                  }
                case PasscodeMismatch =>
                  val incorrectPasscodeForm = TransactorEmailPasscodeForm.form.withError(
                    key = TransactorEmailPasscodeForm.passcodeKey,
                    message = Messages("capture-email-passcode.error.incorrect_passcode")
                  )
                  Future.successful(BadRequest(view(email, incorrectPasscodeForm, isTransactor = true, isNewPasscode = isNewPasscode)))
                case PasscodeNotFound =>
                  Future.successful(Redirect(errorRoutes.EmailPasscodeNotFoundController.show(
                    controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url
                  )))
                case MaxAttemptsExceeded =>
                  Future.successful(Redirect(errorRoutes.EmailPasscodesMaxAttemptsExceededController.show))
              }
            }
        )

  }

  private def getEmailAddress(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[String] = {
    val missingAnswerSection = "tasklist.aboutYou.contactDetails"
    transactorDetailsService.getTransactorDetails.map {
      _.email match {
        case Some(transactorEmail) => transactorEmail
        case None => throw MissingAnswerException(missingAnswerSection)
      }
    }
  }

}
