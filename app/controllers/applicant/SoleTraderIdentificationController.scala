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
import featureswitch.core.config.TaskList
import models.api._
import models.external.BusinessEntity
import models.external.soletraderid.{JourneyLabels, SoleTraderIdJourneyConfig, TranslationLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SoleTraderIdentificationController @Inject()(val sessionService: SessionService,
                                                   val authConnector: AuthConnector,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                                   vatRegistrationService: VatRegistrationService,
                                                   entityService: EntityService
                                                  )(implicit val appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          vatRegistrationService.partyType.flatMap {
            case partyType@(Individual | NETP) =>
              for {
                isTransactor <- vatRegistrationService.isTransactor
                (fullNamePageLabel, welshFullNamePageLabel) = if (isTransactor) {
                  (
                    messagesApi.translate("transactorName.optFullNamePageLabel", Nil)(Lang("en")),
                    messagesApi.translate("transactorName.optFullNamePageLabel", Nil)(Lang("cy"))
                  )
                } else {
                  (None, None)
                }
                config = SoleTraderIdJourneyConfig(
                  continueUrl = appConfig.soleTraderCallbackUrl,
                  optServiceName = messagesApi.translate("service.name", Nil)(Lang("en")),
                  optFullNamePageLabel = fullNamePageLabel,
                  deskProServiceId = appConfig.contactFormServiceIdentifier,
                  signOutUrl = appConfig.feedbackUrl,
                  accessibilityUrl = appConfig.accessibilityStatementUrl,
                  regime = appConfig.regime,
                  businessVerificationCheck = true,
                  labels = Some(JourneyLabels(TranslationLabels(
                    optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy")),
                    optFullNamePageLabel = welshFullNamePageLabel
                  )))
                )
                url <- soleTraderIdentificationService.startSoleTraderJourney(config, partyType)
              } yield {
                Redirect(url)
              }
            case partyType => throw new InternalServerException(
              s"[SoleTraderIdentificationController][startJourney] attempted to start journey with invalid partyType: ${partyType.toString}"
            )
          }
    }

  def callback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          (transactorDetails, soleTrader) <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(transactorDetails)
          _ <- applicantDetailsService.saveApplicantDetails(soleTrader)
        } yield {
          if (isEnabled(TaskList)) {
            Redirect(controllers.routes.TaskListController.show)
          } else {
            Redirect(applicantRoutes.FormerNameController.show)
          }
        }
    }

  def startPartnerJourney: Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          val journeyConfig = SoleTraderIdJourneyConfig(
            continueUrl = appConfig.leadPartnerCallbackUrl,
            optServiceName = messagesApi.translate("service.name", Nil)(Lang("en")),
            deskProServiceId = appConfig.contactFormServiceIdentifier,
            signOutUrl = appConfig.feedbackUrl,
            accessibilityUrl = appConfig.accessibilityStatementUrl,
            regime = appConfig.regime,
            businessVerificationCheck = false,
            labels = Some(JourneyLabels(TranslationLabels(
              optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy"))
            )))
          )

          for {
            entity <- entityService.getEntity(profile.registrationId, 1)
            journeyStartUrl <- soleTraderIdentificationService.startSoleTraderJourney(journeyConfig, entity.partyType)
          } yield {
            SeeOther(journeyStartUrl)
          }
    }

  def partnerCallback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          for {
            (transactorDetails, soleTrader) <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
            _ <- applicantDetailsService.saveApplicantDetails(transactorDetails)
            _ <- entityService.upsertEntity[BusinessEntity](profile.registrationId, 1, soleTrader)
          } yield {
            if (isEnabled(TaskList)) {
              Redirect(controllers.routes.TaskListController.show)
            } else {
              Redirect(applicantRoutes.FormerNameController.show)
            }
          }
    }
}
