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

package controllers.callbacks

import java.io.File

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import views.html.pages.error.TimeoutView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignInOutController @Inject()(val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector)
                                   (implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def postSignIn: Action[AnyContent] = Action.async {
    _ => Future.successful(Redirect(controllers.routes.WelcomeController.show().url))
  }

  def signOut: Action[AnyContent] = Action.async {
    _ => Future.successful(Redirect(appConfig.feedbackUrl).withNewSession)
  }

  def renewSession: Action[AnyContent] = isAuthenticated {
    _ => Future.successful(Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg"))
  }

  def destroySession: Action[AnyContent] = Action.async {
    _ => Future.successful(Redirect(routes.SignInOutController.timeoutShow()).withNewSession)
  }

  def timeoutShow: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(TimeoutView()))
  }
}
