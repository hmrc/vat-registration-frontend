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
import models.Partner
import models.api.{Partnership, Trust}
import models.external.partnershipid.PartnershipIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, PartnershipIdService, SessionProfile, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnershipIdController @Inject()(val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnector,
                                        partnershipIdService: PartnershipIdService,
                                        applicantDetailsService: ApplicantDetailsService,
                                        vatRegistrationService: VatRegistrationService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = PartnershipIdJourneyConfig(
          appConfig.partnershipIdCallbackUrl,
          Some(request2Messages(request)("service.name")),
          appConfig.contactFormServiceIdentifier,
          appConfig.feedbackUrl
        )

        vatRegistrationService.partyType.flatMap {
          case partyType@(Partnership | Trust) => partnershipIdService.createJourney(journeyConfig, partyType).map(
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
          partyType <- vatRegistrationService.partyType
          _ <- applicantDetailsService.saveApplicantDetails(partnershipDetails)
          _ <- if (partyType.equals(Partnership)) applicantDetailsService.saveApplicantDetails(Partner) else Future.successful()
        } yield {
          partyType match {
            case Partnership => Redirect(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType())
            case Trust => Redirect(applicantRoutes.SoleTraderIdentificationController.startJourney())
          }
        }
  }

}
