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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import forms.StartNewApplicationForm
import models.api.trafficmanagement.{ClearTrafficManagementError, TrafficManagementCleared}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, TrafficManagementService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.pages.start_new_application

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartNewApplicationController @Inject()(view: start_new_application,
                                              val authConnector: AuthConnector,
                                              val keystoreConnector: KeystoreConnector,
                                              trafficManagementService: TrafficManagementService)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    Future.successful(
      Ok(view(StartNewApplicationForm.form)))
  }

  def submit: Action[AnyContent] = isAuthenticated { implicit request =>
    StartNewApplicationForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
      startNew =>
        if (startNew) {
          trafficManagementService.clearTrafficManagement map {
            case TrafficManagementCleared =>
              Redirect(routes.WelcomeController.startNewJourney())
            case ClearTrafficManagementError(status) =>
              throw new InternalServerException(s"[StartNewApplicationCtrl] Clear Traffic management API returned status: $status")
          }
        }
        else {
          Future.successful(Redirect(routes.WelcomeController.continueJourney()))
        }
    )
  }
}
