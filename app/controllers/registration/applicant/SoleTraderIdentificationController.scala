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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import models.PartnerEntity
import models.api._
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationController @Inject()(val keystoreConnector: KeystoreConnector,
                                                   val authConnector: AuthConnector,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                                   vatRegistrationService: VatRegistrationService,
                                                   partnersService: PartnersService
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
              soleTraderIdentificationService.startSoleTraderJourney(
                SoleTraderIdJourneyConfig(
                  continueUrl = appConfig.soleTraderCallbackUrl,
                  optServiceName = Some(request2Messages(request)("service.name")),
                  deskProServiceId = appConfig.contactFormServiceIdentifier,
                  signOutUrl = appConfig.feedbackUrl,
                  accessibilityUrl = appConfig.accessibilityStatementUrl,
                  regime = appConfig.regime,
                  businessVerificationCheck = true
                ),
                partyType
              ).map(url => Redirect(url))
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
          Redirect(applicantRoutes.FormerNameController.show)
        }
    }

  def startPartnerJourney(isLeadPartner: Boolean): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          soleTraderIdentificationService.startSoleTraderJourney(
            SoleTraderIdJourneyConfig(
              continueUrl = appConfig.leadPartnerCallbackUrl(isLeadPartner),
              optServiceName = Some(request2Messages(request)("service.name")),
              deskProServiceId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              accessibilityUrl = appConfig.accessibilityStatementUrl,
              regime = appConfig.regime,
              businessVerificationCheck = false
            ),
            Partnership
          ).map(url => Redirect(url))
    }

  def partnerCallback(isLeadPartner: Boolean, journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          (transactorDetails, soleTrader) <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
          _ <- if (isLeadPartner) applicantDetailsService.saveApplicantDetails(transactorDetails) else Future.successful()
          _ <- partnersService.upsertPartner(profile.registrationId, 1, PartnerEntity(soleTrader, Individual, isLeadPartner)) //TODO Figure out indeces for non lead partners
        } yield {
          Redirect(applicantRoutes.FormerNameController.show)
        }
    }
}
