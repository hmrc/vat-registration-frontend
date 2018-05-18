/*
 * Copyright 2018 HM Revenue & Customs
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

package services

import common.enums.VatRegStatus
import connectors.KeystoreConnect
import controllers.routes
import models.CurrentProfile
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait SessionProfile {

  val keystoreConnector: KeystoreConnect

  private val CURRENT_PROFILE_KEY = "CurrentProfile"

  def withCurrentProfile(checkStatus: Boolean = true)(f: CurrentProfile => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    keystoreConnector.fetchAndGet[CurrentProfile](CURRENT_PROFILE_KEY) flatMap { currentProfile =>
      currentProfile.fold(
        Future.successful(Redirect(routes.WelcomeController.show()))
      ) {
        profile => profile.vatRegistrationStatus match {
          case VatRegStatus.draft                                       => f(profile)
          case VatRegStatus.locked if checkStatus => Future.successful(Redirect(routes.ErrorController.submissionRetryable()))
          case (VatRegStatus.held | VatRegStatus.locked) if !checkStatus => f(profile)
          case _                        => Future.successful(Redirect(controllers.callbacks.routes.SignInOutController.postSignIn()))
        }
      }
    }
  }

  def ivPassedCheck(f: => Future[Result])(implicit cp: CurrentProfile, request: Request[_], messages: Messages): Future[Result] = {
    if(!cp.ivPassed.getOrElse(false)) {
      logger.warn(s"[ivPassedCheck] IV has not been passed so showing error page - regId: ${cp.registrationId}, ivpassed: ${cp.ivPassed}")
      Future.successful(InternalServerError(views.html.pages.error.restart()))
    } else {
      f
    }
  }
}
