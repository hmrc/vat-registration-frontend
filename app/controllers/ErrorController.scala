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

import javax.inject.{Inject, Singleton}
import config.AuthClientConnector
import connectors.KeystoreConnect
import play.api.i18n.MessagesApi
import play.api.mvc.Result
import services.{SessionProfile, VatRegistrationService}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import javax.inject.Inject
import common.enums.VatRegStatus
import config.AuthClientConnector
import connectors._
import controllers.builders._
import features.frs.services.FlatRateService
import features.officer.services.LodgingOfficerService
import features.sicAndCompliance.services.SicAndComplianceService
import models.CurrentProfile
import models.api._
import models.view._
import play.api.i18n.MessagesApi
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ErrorControllerImpl @Inject()(config: ServicesConfig,
                                        val authConnector: AuthClientConnector,
                                        val keystoreConnector: KeystoreConnect,
                                        val messagesApi: MessagesApi) extends ErrorController {
}

trait ErrorController extends BaseController with SessionProfile {
  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Ok(views.html.pages.error.submissionTimeout()))
    }
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Ok(views.html.pages.error.submissionFailed()))
      }
  }
}
