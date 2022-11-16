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
import controllers.transactor.{routes => transactorRoutes}
import featureswitch.core.config._
import forms.StartNewApplicationForm
import models.api.EligibilitySubmissionData
import play.api.mvc._
import services._
import views.html.start_new_application

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject()(val vatRegistrationService: VatRegistrationService,
                                  val journeyService: JourneyService,
                                  val authConnector: AuthClientConnector,
                                  val sessionService: SessionService,
                                  view: start_new_application)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticated { implicit request =>
    vatRegistrationService.getAllRegistrations.map {
      case head :: tail => Redirect(routes.ManageRegistrationsController.show)
      case Nil => Redirect(routes.JourneyController.startNewJourney)
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
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
          case _ if header.requiresAttachments => Redirect(controllers.attachments.routes.DocumentsRequiredController.resolve)
          case _ if isEnabled(TaskList) && eligibilitySubmissionData.isDefined => Redirect(controllers.routes.TaskListController.show)
          case _ if header.applicationReference.isEmpty => Redirect(routes.ApplicationReferenceController.show)
          case _ => Redirect(routes.HonestyDeclarationController.show)
        }
      case None =>
        Future.successful(BadRequest("No journey ID was specified"))
    }
  }

  def initJourney(regId: String): Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    (for {
      isTransactor <- vatRegistrationService.isTransactor
      isAgentTransactor = isTransactor && profile.agentReferenceNumber.nonEmpty
      _ <- journeyService.buildCurrentProfile(regId)
    } yield {
      if (isEnabled(TaskList)) {
        Redirect(controllers.routes.TaskListController.show)
      } else {
        if (isAgentTransactor)
          Redirect(transactorRoutes.AgentNameController.show)
        else if (isTransactor)
          Redirect(transactorRoutes.PartOfOrganisationController.show)
        else
          Redirect(controllers.routes.BusinessIdentificationResolverController.resolve)
      }
    }).recover {
      case e: IllegalStateException => Redirect(routes.JourneyController.show)
    }
  }

}
