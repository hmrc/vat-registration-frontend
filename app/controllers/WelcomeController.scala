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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import featureswitch.core.config.SaveAndContinueLater
import play.api.mvc._
import services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WelcomeController @Inject()(val vatRegistrationService: VatRegistrationService,
                                  val currentProfileService: CurrentProfileService,
                                  val authConnector: AuthClientConnector,
                                  val keystoreConnector: KeystoreConnector,
                                  val trafficManagementService: TrafficManagementService,
                                  val saveAndRetrieveService: SaveAndRetrieveService)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    if (isEnabled(SaveAndContinueLater)) {
      trafficManagementService.checkTrafficManagement.flatMap {
        case Failed => Future.successful(Redirect(routes.WelcomeController.startNewJourney()))
        case _ => Future.successful(Redirect(routes.StartNewApplicationController.show()))
      }
    }
    else {
      Future.successful(Redirect(routes.WelcomeController.startNewJourney()))
    }
  }

  def continueJourney: Action[AnyContent] = isAuthenticated { implicit request =>
    trafficManagementService.checkTrafficManagement.flatMap {
      case PassedVatReg(regId) => currentProfileService.buildCurrentProfile(regId).flatMap(_ =>
        saveAndRetrieveService.retrievePartialVatScheme(regId).map(_ =>
          Redirect(appConfig.eligibilityRouteUrl)
        )
      )
      case PassedOTRS => Future.successful(Redirect(appConfig.otrsRoute))
      case Failed => Future.successful(Redirect(routes.WelcomeController.startNewJourney()))
    }
  }

  def startNewJourney: Action[AnyContent] = isAuthenticated { implicit request =>
    getProfile.flatMap {
      case Some(_) => Future.successful(Redirect(appConfig.eligibilityUrl))
      case None => vatRegistrationService.createRegistrationFootprint
        .flatMap(scheme => currentProfileService.buildCurrentProfile(scheme.id))
        .map(_ => Redirect(appConfig.eligibilityUrl))
    }
  }
}
