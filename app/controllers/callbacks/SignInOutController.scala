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
import javax.inject.{Inject, Singleton}

import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.pages.error.TimeoutView

import scala.concurrent.Future

@Singleton
class SignInOutController @Inject()(ds: CommonPlayDependencies,
                                    config: ServicesConfig,
                                    val authConnector: AuthConnector) extends VatRegistrationController(ds) {

  lazy val compRegFEURL = config.getConfString("company-registration-frontend.www.url", "")
  lazy val compRegFEURI = config.getConfString("company-registration-frontend.www.uri", "")

  def postSignIn: Action[AnyContent] = authorised(implicit user => implicit request =>
    Redirect(s"$compRegFEURL$compRegFEURI/post-sign-in")
  )

  def signOut: Action[AnyContent] = authorised { implicit user => implicit request =>
    Redirect(s"$compRegFEURL$compRegFEURI/questionnaire").withNewSession
  }

  def renewSession: Action[AnyContent] = authorised {
    implicit user =>
      implicit request =>
        Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg")
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
