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
import featureswitch.core.config.UseSoleTraderIdentification
import models.api.{CharitableOrg, RegSociety, UkCompany}
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, IncorpIdService, SessionProfile, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IncorpIdController @Inject()(val authConnector: AuthConnector,
                                   val keystoreConnector: KeystoreConnector,
                                   incorpIdService: IncorpIdService,
                                   applicantDetailsService: ApplicantDetailsService,
                                   vatRegistrationService: VatRegistrationService
                                  )(implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = IncorpIdJourneyConfig(
          continueUrl = appConfig.incorpIdCallbackUrl,
          optServiceName = Some(request2Messages(request)("service.name")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime
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
          _ <- applicantDetailsService.saveApplicantDetails(incorpDetails)
        } yield {
          if (isEnabled(UseSoleTraderIdentification)) {
            Redirect(applicantRoutes.IndividualIdentificationController.startJourney)
          }
          else {
            Redirect(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
          }
        }
  }


}
