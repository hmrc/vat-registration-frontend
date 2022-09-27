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

package controllers.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{TaskList, UseSoleTraderIdentification}
import models.api.{CharitableOrg, RegSociety, UkCompany}
import models.external.BusinessEntity
import models.external.incorporatedentityid.{IncorpIdJourneyConfig, JourneyLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IncorpIdController @Inject()(val authConnector: AuthConnector,
                                   val sessionService: SessionService,
                                   incorpIdService: IncorpIdService,
                                   applicantDetailsService: ApplicantDetailsService,
                                   vatRegistrationService: VatRegistrationService,
                                   entityService: EntityService
                                  )(implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = IncorpIdJourneyConfig(
          continueUrl = appConfig.incorpIdCallbackUrl,
          optServiceName = messagesApi.translate("service.name", Nil)(Lang("en")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime,
          businessVerificationCheck = true,
          labels = Some(JourneyLabels(messagesApi.translate("service.name", Nil)(Lang("cy"))))
        )

        vatRegistrationService.partyType.flatMap {
          case partyType@(UkCompany | RegSociety | CharitableOrg) => incorpIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )
          case partyType =>
            throw new InternalServerException(s"[IncorpIdController][startJourney] attempted to start journey with invalid partyType: ${partyType.toString}")
        }

  }

  def incorpIdCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          incorpDetails <- incorpIdService.getDetails(journeyId)
          isTransactor <- vatRegistrationService.isTransactor
          _ <- applicantDetailsService.saveApplicantDetails(incorpDetails)
        } yield {
          if (isEnabled(TaskList)) {
            Redirect(controllers.routes.TaskListController.show)
          } else {
            if (isTransactor || isEnabled(UseSoleTraderIdentification)) {
              Redirect(applicantRoutes.IndividualIdentificationController.startJourney)
            }
            else {
              Redirect(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
            }
          }
        }
  }

  def startPartnerJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = IncorpIdJourneyConfig(
          continueUrl = appConfig.incorpIdPartnerCallbackUrl,
          optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime,
          businessVerificationCheck = false,
          labels = Some(JourneyLabels(messagesApi.translate("service.name", Nil)(Lang("cy"))))
        )

        for {
          entity <- entityService.getEntity(profile.registrationId, 1)
          journeyStartUrl <- incorpIdService.createJourney(journeyConfig, entity.partyType)
        } yield {
          SeeOther(journeyStartUrl)
        }
  }

  def partnerCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          incorpDetails <- incorpIdService.getDetails(journeyId)
          _ <- entityService.upsertEntity[BusinessEntity](profile.registrationId, 1, incorpDetails)
        } yield {
          if (isEnabled(TaskList)) {
            Redirect(controllers.routes.TaskListController.show)
          } else {
            Redirect(applicantRoutes.IndividualIdentificationController.startJourney)
          }
        }
  }
}
