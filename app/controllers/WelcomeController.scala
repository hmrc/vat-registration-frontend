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

import common.enums.VatRegStatus
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import featureswitch.core.config.SaveAndContinueLater
import forms.StartNewApplicationForm
import models.CurrentProfile
import models.api.trafficmanagement.{ClearTrafficManagementError, TrafficManagementCleared}
import play.api.mvc._
import services._
import uk.gov.hmrc.http.InternalServerException
import views.html.pages.start_new_application

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WelcomeController @Inject()(val vatRegistrationService: VatRegistrationService,
                                  val currentProfileService: CurrentProfileService,
                                  val authConnector: AuthClientConnector,
                                  val keystoreConnector: KeystoreConnector,
                                  val trafficManagementService: TrafficManagementService,
                                  val saveAndRetrieveService: SaveAndRetrieveService,
                                  view: start_new_application)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    if (isEnabled(SaveAndContinueLater)) {
      vatRegistrationService.getAllRegistrations.map(_.headOption).flatMap {
        case Some(scheme) if scheme.status == VatRegStatus.draft =>
          currentProfileService.buildCurrentProfile(scheme.id).map { _ =>
            Ok(view(StartNewApplicationForm.form))
          }
        case _ =>
          currentProfileService.keystoreConnector.remove.map { _ =>
            Redirect(routes.WelcomeController.startNewJourney)
          }
      }
    }
    else {
      Future.successful(Redirect(routes.WelcomeController.startNewJourney))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    StartNewApplicationForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      startNew =>
        if (startNew) {
          trafficManagementService.clearTrafficManagement.flatMap {
            case TrafficManagementCleared =>
              vatRegistrationService.deleteVatScheme.map { _ =>
                Redirect(routes.WelcomeController.startNewJourney)
              }
            case ClearTrafficManagementError(status) =>
              throw new InternalServerException(s"[StartNewApplicationCtrl] Clear Traffic management API returned status: $status")
          }
        }
        else {
          Future.successful(Redirect(routes.WelcomeController.continueJourney(Some(profile.registrationId))))
        }
    )
  }

  def startNewJourney: Action[AnyContent] = isAuthenticated { implicit request =>
    getProfile.flatMap {
      case None | Some(CurrentProfile(_, VatRegStatus.held)) => vatRegistrationService.createRegistrationFootprint
        .flatMap(scheme => currentProfileService.buildCurrentProfile(scheme.id))
        .map(_ => Redirect(appConfig.eligibilityUrl))
      case Some(_) => Future.successful(Redirect(appConfig.eligibilityUrl))
    }
  }

  def continueJourney(journey: Option[String]): Action[AnyContent] = isAuthenticated { implicit request =>
    journey match {
      case Some(regId: String) =>
        trafficManagementService.checkTrafficManagement(regId).flatMap {
          case PassedVatReg(regId) => saveAndRetrieveService.retrievePartialVatScheme(regId)
            .flatMap(_ => currentProfileService.buildCurrentProfile(regId))
            .map(_ => Redirect(appConfig.eligibilityRouteUrl))
          case PassedOTRS => Future.successful(Redirect(appConfig.otrsRoute))
          case Failed => Future.successful(Redirect(routes.WelcomeController.startNewJourney))
        }
      case None =>
        Future.successful(BadRequest("No journey ID was specified"))
    }
  }

}
