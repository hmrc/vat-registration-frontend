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
import features.returns.ReturnsService
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{RegistrationService, SessionProfile}
import views.html.pages.application_submission_confirmation

class ApplicationSubmissionControllerImpl @Inject()(val vatRegService: RegistrationService,
                                                    val returnsService:  ReturnsService,
                                                    val authConnector: AuthClientConnector,
                                                    val keystoreConnector: KeystoreConnect,
                                                    val messagesApi: MessagesApi) extends ApplicationSubmissionController

trait ApplicationSubmissionController extends BaseController with SessionProfile {
  val vatRegService: RegistrationService
  val returnsService: ReturnsService

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          ackRef <- vatRegService.getAckRef(profile.registrationId)
          returns <- returnsService.getReturns
        } yield Ok(application_submission_confirmation(ackRef, returns.staggerStart))
      }
  }
}
