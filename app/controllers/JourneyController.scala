/*
 * Copyright 2023 HM Revenue & Customs
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
import models.api.EligibilitySubmissionData
import play.api.mvc._
import services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject()(val vatRegistrationService: VatRegistrationService,
                                  val journeyService: JourneyService,
                                  val authConnector: AuthClientConnector,
                                  val sessionService: SessionService)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    vatRegistrationService.getAllRegistrations.map {
      case _ :: _ => Redirect(routes.ManageRegistrationsController.show)
      case Nil => Redirect(routes.JourneyController.startNewJourney)
    }
  }

  def startNewJourney: Action[AnyContent] = isAuthenticated { implicit request =>
    for {
      scheme <- vatRegistrationService.createRegistrationFootprint
      _ <- journeyService.buildCurrentProfile(scheme.registrationId)
    } yield Redirect(routes.ApplicationReferenceController.show)
  }

  // scalastyle:off
  def continueJourney(journey: Option[String]): Action[AnyContent] = isAuthenticated { implicit request =>
    journey match {
      case Some(regId: String) =>
        for {
          _ <- journeyService.buildCurrentProfile(regId)
          header <- vatRegistrationService.getVatSchemeHeader(regId)
          eligibilitySubmissionData <- vatRegistrationService.getSection[EligibilitySubmissionData](regId)
        } yield header.status match {
          case VatRegStatus.submitted => Redirect(routes.ApplicationSubmissionController.show)
          case _ if header.applicationReference.isEmpty => Redirect(routes.ApplicationReferenceController.show)
          case _ if header.confirmInformationDeclaration.isEmpty => Redirect(routes.HonestyDeclarationController.show)
          case _ if header.requiresAttachments => Redirect(controllers.attachments.routes.DocumentsRequiredController.resolve)
          case _ if eligibilitySubmissionData.isDefined => Redirect(controllers.routes.TaskListController.show)
          case _ => Redirect(appConfig.eligibilityStartUrl(regId))
        }
      case None =>
        Future.successful(BadRequest("No journey ID was specified"))
    }
  }

  def initJourney(regId: String): Action[AnyContent] = isAuthenticatedWithProfile { implicit request => _ =>
    journeyService.buildCurrentProfile(regId)
      .map { _ =>
        Redirect(controllers.routes.TaskListController.show)
      }
      .recover {
        case _ => Redirect(routes.JourneyController.show)
      }
  }

}
