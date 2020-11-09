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

package controllers.callbacks

import java.io.File

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionProfile
import views.html.pages.error.TimeoutView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignInOutController @Inject()(mcc: MessagesControllerComponents,
                                    val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector)
                                   (implicit val appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def postSignIn: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(controllers.routes.WelcomeController.show().url))
  }

  def signOut: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Redirect(appConfig.feedbackUrl).withNewSession)
  }

  def renewSession: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg"))
  }

  def destroySession: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(routes.SignInOutController.timeoutShow()).withNewSession)
  }

  def timeoutShow: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(TimeoutView()))
  }

  def errorShow: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(InternalServerError(views.html.pages.error.restart()))
  }
}
