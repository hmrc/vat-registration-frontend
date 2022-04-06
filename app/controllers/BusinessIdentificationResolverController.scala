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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.applicant.{routes => applicantRoutes}
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton
class BusinessIdentificationResolverController @Inject()(val sessionService: SessionService,
                                                         val authConnector: AuthConnector,
                                                         vatRegistrationService: VatRegistrationService
                                                        )(implicit val appConfig: FrontendAppConfig,
                                                          val executionContext: ExecutionContext,
                                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def resolve: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          partyType <- vatRegistrationService.partyType
        } yield {
          partyType match {
            case Individual | NETP =>
              Redirect(applicantRoutes.SoleTraderIdentificationController.startJourney)
            case UkCompany | RegSociety | CharitableOrg =>
              Redirect(applicantRoutes.IncorpIdController.startJourney)
            case Partnership | ScotPartnership | ScotLtdPartnership | LtdPartnership | LtdLiabilityPartnership =>
              Redirect(applicantRoutes.PartnershipIdController.startJourney)
            case UnincorpAssoc | Trust | NonUkNonEstablished =>
              Redirect(applicantRoutes.MinorEntityIdController.startJourney)
          }
        }
  }
}
