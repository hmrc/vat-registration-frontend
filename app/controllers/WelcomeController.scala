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
import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{CurrentProfileService, RegistrationService, SessionProfile}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import views.html.pages.welcome

import scala.concurrent.Future

class WelcomeControllerImpl @Inject()(val vatRegistrationService: RegistrationService,
                                      val currentProfileService: CurrentProfileService,
                                      val authConnector: AuthClientConnector,
                                      val keystoreConnector: KeystoreConnector,
                                      val messagesApi: MessagesApi,
                                      conf: ServicesConfig) extends WelcomeController {
  val eligibilityFEUrl = conf.getConfString("vat-registration-eligibility-frontend.uri", throw new Exception("[WelcomeController] Could not find microservice.services.vat-registration-eligibility-frontend.uri"))

  override val eligibilityFE: Call = Call(method = "GET", url = eligibilityFEUrl)
}

trait WelcomeController extends BaseController with SessionProfile {
  val vatRegistrationService: RegistrationService
  val currentProfileService: CurrentProfileService

  val eligibilityFE: Call

  def show: Action[AnyContent] = Action(implicit request => Redirect(routes.WelcomeController.start()))

  def start: Action[AnyContent] = isAuthenticated {
    implicit request =>
      for {
        registrationId <- vatRegistrationService.createRegistrationFootprint
        _ <- currentProfileService.buildCurrentProfile(registrationId)
        taxableThreshold <- vatRegistrationService.getTaxableThreshold()
      } yield {
        Ok(welcome(taxableThreshold))
      }
  }


  def redirectToEligibility: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      _ =>
        Future.successful(Redirect(eligibilityFE))
  }
}
