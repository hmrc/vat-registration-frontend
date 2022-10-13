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

package controllers.grs

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.applicant.{routes => applicantRoutes}
import controllers.partners.PartnerIndexValidation
import featureswitch.core.config.TaskList
import models.Entity
import models.Entity.leadEntityIndex
import models.external.BusinessEntity
import models.external.soletraderid.{JourneyLabels, SoleTraderIdJourneyConfig, TranslationLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnerSoleTraderIdController @Inject()(val sessionService: SessionService,
                                              val authConnector: AuthConnector,
                                              val applicantDetailsService: ApplicantDetailsService,
                                              soleTraderIdentificationService: SoleTraderIdentificationService,
                                              val entityService: EntityService,
                                              vatRegistrationService: VatRegistrationService
                                             )(implicit val appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  def startJourney(index: Int): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          validateIndex(index, routes.PartnerSoleTraderIdController.startJourney, minIndex = 1) {
            case Some(Entity(_, partyType, _, _, _, _, _)) =>
              for {
                isTransactor <- vatRegistrationService.isTransactor
                (fullNamePageLabel, welshFullNamePageLabel) = if (isTransactor && index == leadEntityIndex) {
                  (
                    messagesApi.translate("transactorName.leadPartner.optFullNamePageLabel", Nil)(Lang("en")),
                    messagesApi.translate("transactorName.leadPartner.optFullNamePageLabel", Nil)(Lang("cy"))
                  )
                } else if (index > leadEntityIndex) {
                  (
                    messagesApi.translate("transactorName.partner.optFullNamePageLabel", Seq(index))(Lang("en")),
                    messagesApi.translate("transactorName.partner.optFullNamePageLabel", Seq(index))(Lang("cy"))
                  )
                } else {
                  (None, None)
                }
                journeyConfig = SoleTraderIdJourneyConfig(
                  continueUrl = appConfig.leadPartnerCallbackUrl(index),
                  optServiceName = messagesApi.translate("service.name", Nil)(Lang("en")),
                  optFullNamePageLabel = fullNamePageLabel,
                  deskProServiceId = appConfig.contactFormServiceIdentifier,
                  signOutUrl = appConfig.feedbackUrl,
                  accessibilityUrl = appConfig.accessibilityStatementUrl,
                  regime = appConfig.regime,
                  businessVerificationCheck = false,
                  labels = Some(JourneyLabels(TranslationLabels(
                    optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy")),
                    optFullNamePageLabel = welshFullNamePageLabel
                  )))
                )
                journeyStartUrl <- soleTraderIdentificationService.startSoleTraderJourney(journeyConfig, partyType)
              } yield {
                SeeOther(journeyStartUrl)
              }
            case _ =>
              throw new InternalServerException(s"[PartnerPartnershipIdController] Missing entity with partyType for entity: $index")
          }
    }

  def callback(index: Int, journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          validateIndexSubmit(index, routes.PartnerSoleTraderIdController.startJourney, minIndex = 1) {
            for {
              (transactorDetails, soleTrader) <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
              _ <- if (index == leadEntityIndex) applicantDetailsService.saveApplicantDetails(transactorDetails) else Future.successful()
              _ <- entityService.upsertEntity[BusinessEntity](profile.registrationId, index, soleTrader)
            } yield {
              if (index == leadEntityIndex) {
                if (isEnabled(TaskList)) {
                  Redirect(controllers.routes.TaskListController.show)
                } else {
                  Redirect(applicantRoutes.FormerNameController.show)
                }
              } else {
                Redirect(controllers.partners.routes.PartnerAddressController.redirectToAlf(index))
              }
            }
          }
    }
}
