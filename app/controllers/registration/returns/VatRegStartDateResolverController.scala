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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.external.{IncorporatedEntity, PartnershipIdEntity}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class VatRegStartDateResolverController @Inject()(val sessionService: SessionService,
                                                  val authConnector: AuthClientConnector,
                                                  val vatRegistrationService: VatRegistrationService)
                                                 (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def resolve: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      for {
        scheme <- vatRegistrationService.getVatScheme
        eligibilityData = scheme.eligibilitySubmissionData
          .getOrElse(throw new InternalServerException("[ClaimRefundsController] Unable to retrieve eligibility data"))
        incorpDate = scheme.applicantDetails.flatMap {
          _.entity.flatMap {
            case incorpEntity: IncorporatedEntity => incorpEntity.dateOfIncorporation
            case partnershipEntity: PartnershipIdEntity => partnershipEntity.dateOfIncorporation
            case _ => None
          }
        }
        isVoluntary = !eligibilityData.threshold.mandatoryRegistration
      } yield (isVoluntary, incorpDate) match {
        case (true, None) => Redirect(routes.VoluntaryStartDateNoChoiceController.show)
        case (true, _) => Redirect(routes.ReturnsController.voluntaryStartPage)
        case (false, _) => Redirect(routes.ReturnsController.mandatoryStartPage)
      }
  }

}
