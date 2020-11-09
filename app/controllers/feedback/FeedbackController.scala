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

package controllers.feedback

import java.net.URLEncoder

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.SessionProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeedbackController @Inject()(mcc: MessagesControllerComponents,
                                   val authConnector: AuthClientConnector,
                                   val keystoreConnector: KeystoreConnector)
                                  (implicit val appConfig: FrontendAppConfig,
                                   val executionContext: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  lazy val contactFrontendPartialBaseUrl: String = appConfig.contactFrontendPartialBaseUrl
  lazy val contactFormServiceIdentifier: String = appConfig.contactFormServiceIdentifier

  def contactFormReferrer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")

  private def feedbackFormUrl(implicit request: Request[AnyContent]) =
    s"$contactFrontendPartialBaseUrl/contact/beta-feedback?backUrl=${urlEncode(contactFormReferrer)}" +
      s"&service=$contactFormServiceIdentifier"

  def feedbackShow: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(feedbackFormUrl))
  }

  private def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")
}
