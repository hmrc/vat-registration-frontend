/*
 * Copyright 2019 HM Revenue & Customs
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
import features.returns.services.ReturnsService
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{RegistrationService, SessionProfile}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import views.html.pages.application_submission_confirmation

class ApplicationSubmissionControllerImpl @Inject()(val vatRegService: RegistrationService,
                                                    val returnsService:  ReturnsService,
                                                    val authConnector: AuthClientConnector,
                                                    val keystoreConnector: KeystoreConnector,
                                                    val messagesApi: MessagesApi,
                                                    val config: ServicesConfig) extends ApplicationSubmissionController {

  lazy val compRegFEURL = config.getConfString("company-registration-frontend.www.url",
    throw new Exception("Config: company-registration-frontend.www.url not found"))

  lazy val compRegFEURI = config.getConfString("company-registration-frontend.www.uri",
    throw new Exception("Config: company-registration-frontend.www.uri not found"))

  lazy val compRegFEDashboard = config.getConfString("company-registration-frontend.www.dashboard",
    throw new Exception("Config: company-registration-frontend.www.dashboard not found"))

  lazy val dashboardUrl = s"$compRegFEURL$compRegFEURI$compRegFEDashboard"
}

trait ApplicationSubmissionController extends BaseController with SessionProfile {
  val vatRegService       :RegistrationService
  val returnsService      :ReturnsService
  val dashboardUrl        :String

  def show: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => implicit profile =>
      for {
        ackRef <- vatRegService.getAckRef(profile.registrationId)
        returns <- returnsService.getReturns
      } yield Ok(application_submission_confirmation(ackRef, returns.staggerStart,dashboardUrl))
  }
}
