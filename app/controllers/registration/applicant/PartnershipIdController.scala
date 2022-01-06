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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import models.api._
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.{Partner, PartnerEntity}
import play.api.mvc.{Action, AnyContent}
import services.SessionService.{leadPartnerEntityKey, scottishPartnershipNameKey}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnershipIdController @Inject()(val authConnector: AuthConnector,
                                        val sessionService: SessionService,
                                        partnershipIdService: PartnershipIdService,
                                        applicantDetailsService: ApplicantDetailsService,
                                        vatRegistrationService: VatRegistrationService,
                                        partnersService: PartnersService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = PartnershipIdJourneyConfig(
          continueUrl = appConfig.partnershipIdCallbackUrl,
          optServiceName = Some(request2Messages(request)("service.name")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime,
          businessVerificationCheck = true
        )

        vatRegistrationService.partyType.flatMap {
          case partyType@Partnership => partnershipIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )
          case partyType@ScotPartnership => partnershipIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )
          case partyType@ScotLtdPartnership => partnershipIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )
          case partyType@LtdPartnership => partnershipIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )

          case partyType => throw new InternalServerException(
            s"[PartnershipIdController][startJourney] attempted to start journey with invalid partyType: ${partyType.toString}"
          )
        }
  }

  def callback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          partnershipDetails <- partnershipIdService.getDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(partnershipDetails)
          _ <- applicantDetailsService.saveApplicantDetails(Partner)
        } yield {
          Redirect(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType)
        }
  }

  def startPartnerJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = PartnershipIdJourneyConfig(
          continueUrl = appConfig.partnershipIdPartnerCallbackUrl,
          optServiceName = Some(request2Messages(request)("service.name")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime,
          businessVerificationCheck = false
        )

        for {
          partyType <- sessionService.fetchAndGet[PartyType](leadPartnerEntityKey).map(
            _.getOrElse(throw new InternalServerException("[PartnershipIdController][startPartnerJourney] no lead partner party type in session during journey start"))
          )
          journeyStartUrl <- partnershipIdService.createJourney(journeyConfig, partyType)
        } yield {
          SeeOther(journeyStartUrl)
        }
  }

  def partnerCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          partnerDetails <- partnershipIdService.getDetails(journeyId)
          partyType <- sessionService.fetchAndGet[PartyType](leadPartnerEntityKey).map(
            _.getOrElse(throw new InternalServerException("[PartnershipIdController][partnerCallback] no lead partner party type in session during callback"))
          )
          optScottishPartnershipName <- sessionService.fetchAndGet[String](scottishPartnershipNameKey)
          updatedPartnerDetails <- Future.successful(
            if (partyType.equals(ScotPartnership)) {
              partnerDetails.copy(companyName = optScottishPartnershipName)
            } else {
              partnerDetails
            }
          )
          _ <- partnersService.upsertPartner(profile.registrationId, 1, PartnerEntity(updatedPartnerDetails, partyType, isLeadPartner = true))
        } yield {
          Redirect(applicantRoutes.IndividualIdentificationController.startJourney)
        }
  }
}
