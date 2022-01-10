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
import featureswitch.core.config.ShortOrgName
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradingNameResolverController @Inject()(val sessionService: SessionService,
                                              val authConnector: AuthConnector,
                                              vatRegistrationService: VatRegistrationService,
                                              applicantDetailsService: ApplicantDetailsService
                                             )(implicit val appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  //scalastyle:off
  def resolve: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatRegistrationService.partyType.flatMap {
          case Individual | NETP =>
            Future.successful(Redirect(controllers.registration.business.routes.MandatoryTradingNameController.show))
          case Partnership | ScotPartnership =>
            Future.successful(Redirect(controllers.registration.business.routes.PartnershipNameController.show))
          case UkCompany | RegSociety | CharitableOrg | Trust | UnincorpAssoc | NonUkNonEstablished =>
            applicantDetailsService.getCompanyName.map {
              case Some(companyName) if companyName.length > 105 & isEnabled(ShortOrgName) => Redirect(controllers.registration.business.routes.ShortOrgNameController.show)
              case Some(_) => Redirect(controllers.registration.business.routes.TradingNameController.show)
              case None => Redirect(controllers.registration.business.routes.BusinessNameController.show)
            }
          case ScotLtdPartnership | LtdPartnership | LtdLiabilityPartnership =>
            applicantDetailsService.getCompanyName.map {
              case Some(companyName) if companyName.length > 105 & isEnabled(ShortOrgName) => Redirect(controllers.registration.business.routes.ShortOrgNameController.show)
              case Some(_) => Redirect(controllers.registration.business.routes.TradingNameController.show)
              case None => Redirect(controllers.registration.business.routes.PartnershipNameController.show)
            }
          case pt => throw new InternalServerException(s"PartyType: $pt not supported")
        }
  }
}
