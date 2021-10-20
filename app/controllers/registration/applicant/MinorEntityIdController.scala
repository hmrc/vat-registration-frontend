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
import models.api.{NonUkNonEstablished, Trust, UnincorpAssoc}
import models.external.minorentityid.MinorEntityIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, MinorEntityIdService, SessionProfile, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MinorEntityIdController @Inject()(val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnector,
                                        minorEntityIdService: MinorEntityIdService,
                                        applicantDetailsService: ApplicantDetailsService,
                                        vatRegistrationService: VatRegistrationService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = MinorEntityIdJourneyConfig(
          appConfig.minorEntityIdCallbackUrl,
          Some(request2Messages(request)("service.name")),
          appConfig.contactFormServiceIdentifier,
          appConfig.feedbackUrl,
          appConfig.accessibilityStatementUrl
        )

        vatRegistrationService.partyType.flatMap {
          case partyType@(Trust | UnincorpAssoc | NonUkNonEstablished) => minorEntityIdService.createJourney(journeyConfig, partyType).map(
            journeyStartUrl => SeeOther(journeyStartUrl)
          )
          case partyType => throw new InternalServerException(
            s"[MinorEntityIdController][startJourney] attempted to start journey with invalid partyType: ${partyType.toString}"
          )
        }
  }

  def callback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- minorEntityIdService.getDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(businessDetails)
        } yield {
          Redirect(applicantRoutes.IndividualIdentificationController.startJourney())
        }
  }

}
