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

import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnector
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.Future

class ErrorControllerImpl @Inject()(config: ServicesConfig,
                                    val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector,
                                    val messagesApi: MessagesApi) extends ErrorController {

  lazy val compRegFEURL = config.getConfString("company-registration-frontend.www.url",
    throw new Exception("Config: company-registration-frontend.www.url not found"))

  lazy val compRegFEURI = config.getConfString("company-registration-frontend.www.uri",
    throw new Exception("Config: company-registration-frontend.www.uri not found"))

  lazy val compRegFERejected = config.getConfString("company-registration-frontend.www.rejected",
    throw new Exception("Config: company-registration-frontend.www.rejected not found"))

  override lazy val rejectedUrl = s"$compRegFEURL$compRegFEURI$compRegFERejected"

}

trait ErrorController extends BaseController with SessionProfile {

  val rejectedUrl: String

  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.pages.error.submissionTimeout()))
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.pages.error.submissionFailed()))
  }

  def redirectToCR() = Action(Redirect(rejectedUrl))
}
