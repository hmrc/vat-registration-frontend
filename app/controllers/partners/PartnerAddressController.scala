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

package controllers.partners

import common.enums.AddressLookupJourneyIdentifier.{companyPartner, individualPartner}
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.Entity
import models.api.{Address, Individual}
import models.external.{IncorporatedEntity, PartnershipIdEntity, SoleTraderIdEntity}
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PartnerAddressController @Inject()(val sessionService: SessionService,
                                            val authConnector: AuthClientConnector,
                                            val entityService: EntityService,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            val vatRegistrationService: VatRegistrationService,
                                            addressLookupService: AddressLookupService)
                                           (implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  def redirectToAlf(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.PartnerAddressController.redirectToAlf) {
          case Some(Entity(Some(soleTrader: SoleTraderIdEntity), Individual, _, _, _, _, _)) =>
            addressLookupService.getJourneyUrl(
              individualPartner,
              routes.PartnerAddressController.addressLookupCallback(index),
              optName = Some(soleTrader.firstName)
            ).map(Redirect)
          case Some(Entity(Some(business), _, _, _, _, _, _)) =>
            val companyName = business match {
              case incorpBusiness: IncorporatedEntity => incorpBusiness.companyName
              case partnershipBusiness: PartnershipIdEntity => partnershipBusiness.companyName
            }

            addressLookupService.getJourneyUrl(
              companyPartner,
              routes.PartnerAddressController.addressLookupCallback(index),
              useUkMode = true,
              optName = companyName
            ).map(Redirect)
          case _ =>
            throw new InternalServerException("[PartnerAddressController] Attempted to go down partner alf journey without capturing partyType or passing GRS")
        }
  }

  def addressLookupCallback(index: Int, id: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerAddressController.redirectToAlf) {
          for {
            address <- addressLookupService.getAddressById(id)
            _ <- entityService.upsertEntity[Address](profile.registrationId, index, address.normalise())
          } yield NotImplemented
        }
  }
}
