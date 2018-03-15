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
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{CurrentProfileSrv, RegistrationService, SessionProfile}
import views.html.pages.welcome

import scala.concurrent.Future

class WelcomeControllerImpl @Inject()(val vatRegistrationService: RegistrationService,
                                      val currentProfileService: CurrentProfileSrv,
                                      val authConnector: AuthClientConnector,
                                      val keystoreConnector: KeystoreConnect,
                                      val conf: Configuration,
                                      val messagesApi: MessagesApi) extends WelcomeController {
  val eligibilityFEUrl = conf.getString("microservice.services.vat-registration-eligibility-frontend.entry-url").getOrElse("")
  override val eligibilityFE: Call = Call(method = "GET", url = eligibilityFEUrl)
}

trait WelcomeController extends BaseController with SessionProfile {
  val vatRegistrationService: RegistrationService
  val currentProfileService: CurrentProfileSrv

  val eligibilityFE: Call

  def show: Action[AnyContent] = Action(implicit request => Redirect(routes.WelcomeController.start()))

  def start: Action[AnyContent] = isAuthenticated {
    implicit request =>
      vatRegistrationService.assertFootprintNeeded flatMap {
        case Some((regId, txID)) =>
          currentProfileService.buildCurrentProfile(regId, txID) map { _ =>
            Ok(welcome())
          }
        case None => Future.successful(Redirect(controllers.callbacks.routes.SignInOutController.postSignIn()))
      }
  }

  def redirectToEligibility: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => _ =>
      Future.successful(Redirect(eligibilityFE))
  }
}
