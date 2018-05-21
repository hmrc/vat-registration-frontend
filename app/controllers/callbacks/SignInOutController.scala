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

package controllers.callbacks

import java.io.File
import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnector
import controllers.BaseController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import uk.gov.hmrc.play.config.inject.ServicesConfig
import views.html.pages.error.TimeoutView

import scala.concurrent.Future

class SignInOutControllerImpl @Inject()(config: ServicesConfig,
                                        val authConnector: AuthClientConnector,
                                        val keystoreConnector: KeystoreConnector,
                                        val messagesApi: MessagesApi) extends SignInOutController {

  lazy val compRegFEURL = config.getConfString("company-registration-frontend.www.url",
    throw new Exception("Config: company-registration-frontend.www.url not found"))

  lazy val compRegFEURI = config.getConfString("company-registration-frontend.www.uri",
    throw new Exception("Config: company-registration-frontend.www.uri not found"))

  lazy val compRegFEQuestionnaire = config.getConfString("company-registration-frontend.www.questionnaire",
    throw new Exception("Config: company-registration-frontend.www.questionnaire not found"))

  lazy val compRegFEPostSignIn = config.getConfString("company-registration-frontend.www.post-sign-in",
    throw new Exception("Config: company-registration-frontend.www.post-sign-in not found"))


}

trait SignInOutController extends BaseController with SessionProfile {

  val compRegFEURI           : String
  val compRegFEURL           : String
  val compRegFEQuestionnaire : String
  val compRegFEPostSignIn    : String

  def postSignIn: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(s"$compRegFEURL$compRegFEURI$compRegFEPostSignIn"))
  }

  def signOut: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Redirect(s"$compRegFEURL$compRegFEURI$compRegFEQuestionnaire").withNewSession)
  }

  def renewSession: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg"))
  }

  def destroySession: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(routes.SignInOutController.timeoutShow()).withNewSession)
  }

  def timeoutShow = Action.async {
    implicit request =>
      Future.successful(Ok(TimeoutView()))
  }
  
  def errorShow = Action.async{
    implicit request =>
      Future.successful(InternalServerError(views.html.pages.error.restart()))
  }
}
