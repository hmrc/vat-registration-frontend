/*
 * Copyright 2022 HM Revenue & Customs
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
import featureswitch.core.config.{FullAgentJourney, MultipleRegistrations, SaveAndContinueLater, TrafficManagementPredicate}
import forms.StartNewApplicationForm
import play.api.mvc._
import services.{SessionService, _}
import views.html.pages.start_new_application
import controllers.registration.transactor.{routes => transactorRoutes}
import models.api.VatSchemeHeader
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject()(val vatRegistrationService: VatRegistrationService,
                                  val journeyService: JourneyService,
                                  val authConnector: AuthClientConnector,
                                  val sessionService: SessionService,
                                  val trafficManagementService: TrafficManagementService,
                                  val saveAndRetrieveService: SaveAndRetrieveService,
                                  view: start_new_application)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    if (isEnabled(SaveAndContinueLater)) {
      if (isEnabled(MultipleRegistrations)) {
        vatRegistrationService.getAllRegistrations.map {
          case head :: tail => Redirect(routes.ManageRegistrationsController.show)
          case Nil => Redirect(routes.JourneyController.startNewJourney)
        }
      } else {
        vatRegistrationService.getAllRegistrations.map(_.lastOption).flatMap {
          case Some(header) if header.status == VatRegStatus.draft =>
            journeyService.buildCurrentProfile(header.registrationId).map { _ =>
              Ok(view(StartNewApplicationForm.form))
            }.recover { //This handles the rare case where build current profile status check is applied to an old unparsable VatScheme
              case exception =>
                Redirect(routes.JourneyController.startNewJourney)
            }
          case _ =>
            journeyService.sessionService.remove.map { _ =>
              Redirect(routes.JourneyController.startNewJourney)
            }
        }
      }

    }
    else {
      Future.successful(Redirect(routes.JourneyController.startNewJourney))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) { implicit request => implicit profile =>
    StartNewApplicationForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      startNew =>
        if (startNew) {
          Future.successful(Redirect(routes.JourneyController.startNewJourney))
        } else {
          Future.successful(Redirect(routes.JourneyController.continueJourney(Some(profile.registrationId))))
        }
    )
  }

  def startNewJourney: Action[AnyContent] = isAuthenticated { implicit request =>
    for {
      scheme <- vatRegistrationService.createRegistrationFootprint
      _ <- journeyService.buildCurrentProfile(scheme.id)
    } yield if (isEnabled(MultipleRegistrations)) {
      Redirect(routes.ApplicationReferenceController.show)
    } else {
      Redirect(routes.HonestyDeclarationController.show)
    }
  }

  // scalastyle:off
  def continueJourney(journey: Option[String]): Action[AnyContent] = isAuthenticated { implicit request =>
    journey match {
      case Some(regId: String) =>
        for {
          _ <- journeyService.buildCurrentProfile(regId)
          optHeader <- vatRegistrationService.getVatSchemeJson(regId).map(_.validate[VatSchemeHeader].asOpt)
          header = optHeader.getOrElse(throw new InternalServerException(s"[continueJourney] couldn't parse vat scheme header for regId: $regId"))
          trafficManagementResponse <- trafficManagementService.checkTrafficManagement(regId) // Used to check if user is OTRS so should always be enabled
        } yield (header.status, trafficManagementResponse) match {
          case (_, PassedOTRS) => Redirect(appConfig.otrsRoute)
          case (VatRegStatus.submitted, _) => Redirect(routes.ApplicationSubmissionController.show)
          case _ if header.requiresAttachments => Redirect(controllers.registration.attachments.routes.DocumentsRequiredController.resolve)
          case _ if isEnabled(MultipleRegistrations) => Redirect(routes.ApplicationReferenceController.show)
          case _ => Redirect(routes.HonestyDeclarationController.show)
        }
      case None =>
        Future.successful(BadRequest("No journey ID was specified"))
    }
  }

  def initJourney(regId: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    (for {
      isTransactor <- vatRegistrationService.isTransactor
      isAgent = isTransactor && profile.agentReferenceNumber.nonEmpty
      _ <- journeyService.buildCurrentProfile(regId)
    } yield {
      if (isTransactor && !isAgent)
        Redirect(transactorRoutes.PartOfOrganisationController.show)
      else if (isAgent && isEnabled(FullAgentJourney))
        Redirect(transactorRoutes.AgentNameController.show)
      else
        Redirect(controllers.routes.BusinessIdentificationResolverController.resolve)
    }).recover {
      case e: IllegalStateException => Redirect(routes.JourneyController.show)
    }
  }

}
