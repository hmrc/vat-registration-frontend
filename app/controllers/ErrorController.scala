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

import config.AuthClientConnector
import connectors.KeystoreConnector
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.Future

@Singleton
class ErrorController @Inject()(config: ServicesConfig,
                                val authConnector: AuthClientConnector,
                                val keystoreConnector: KeystoreConnector
                               )(implicit val messagesApi: MessagesApi) extends BaseController with SessionProfile {

  lazy val compRegFEURL: String = config.getConfString("company-registration-frontend.www.url",
    throw new Exception("Config: company-registration-frontend.www.url not found"))

  lazy val compRegFEURI: String = config.getConfString("company-registration-frontend.www.uri",
    throw new Exception("Config: company-registration-frontend.www.uri not found"))

  lazy val compRegFERejected: String = config.getConfString("company-registration-frontend.www.rejected",
    throw new Exception("Config: company-registration-frontend.www.rejected not found"))

  lazy val rejectedUrl = s"$compRegFEURL$compRegFEURI$compRegFERejected"

  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(views.html.pages.error.submissionTimeout()))
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(views.html.pages.error.submissionFailed()))
  }

  def redirectToCR(): Action[AnyContent] = Action(Redirect(rejectedUrl))
}
