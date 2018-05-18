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

package controllers

import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnect
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.Future

class ErrorControllerImpl @Inject()(config: ServicesConfig,
                                        val authConnector: AuthClientConnector,
                                        val keystoreConnector: KeystoreConnect,
                                        val messagesApi: MessagesApi) extends ErrorController {
}

trait ErrorController extends BaseController with SessionProfile {
  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.pages.error.submissionTimeout()))
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.pages.error.submissionFailed()))
  }
}
