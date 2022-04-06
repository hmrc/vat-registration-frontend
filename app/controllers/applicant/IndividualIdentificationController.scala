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
import models.api.{LtdLiabilityPartnership, LtdPartnership, Partnership, ScotLtdPartnership, ScotPartnership}
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, _}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IndividualIdentificationController @Inject()(val sessionService: SessionService,
                                                   val authConnector: AuthConnector,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                                   vatRegistrationService: VatRegistrationService
                                                  )(implicit val appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          for {
            isTransactor <- vatRegistrationService.isTransactor
            fullNamePageLabel = isTransactor match {
              case true => Some(request2Messages(request)("transactorName.optFullNamePageLabel"))
              case _ => None
            }
            config = SoleTraderIdJourneyConfig(
              continueUrl = appConfig.individualCallbackUrl,
              optServiceName = Some(request2Messages(request)("service.name")),
              optFullNamePageLabel = fullNamePageLabel,
              deskProServiceId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              accessibilityUrl = appConfig.accessibilityStatementUrl,
              regime = appConfig.regime,
              businessVerificationCheck = true
            )
            url <- soleTraderIdentificationService.startIndividualJourney(config)
          } yield {
            Redirect(url)
          }
    }

  def callback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          individualDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(individualDetails)
          partyType <- vatRegistrationService.partyType
        } yield partyType match {
          case Partnership | ScotPartnership | LtdPartnership | ScotLtdPartnership | LtdLiabilityPartnership =>
            Redirect(applicantRoutes.FormerNameController.show)
          case _ => Redirect(applicantRoutes.CaptureRoleInTheBusinessController.show)
        }
    }
}
