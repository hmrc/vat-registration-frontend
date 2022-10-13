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
import models.api.Address
import models.external.SoleTraderIdEntity
import play.api.mvc.{Action, AnyContent}
import services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

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
          case Some(entity) if entity.displayName.isDefined =>
            val isCompany = entity.details match {
              case Some(_: SoleTraderIdEntity) => false
              case _ => true
            }

            addressLookupService.getJourneyUrl(
              if (isCompany) companyPartner else individualPartner,
              routes.PartnerAddressController.addressLookupCallback(index),
              useUkMode = isCompany,
              optName = entity.displayName
            ).map(Redirect)
          case _ =>
            logger.warn("[PartnerAddressController] Attempted to go down partner alf journey without capturing partyType or passing GRS")
            Future.successful(Redirect(routes.PartnerEntityTypeController.showPartnerType(index)))
        }
  }

  def addressLookupCallback(index: Int, id: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerAddressController.redirectToAlf) {
          for {
            address <- addressLookupService.getAddressById(id)
            _ <- entityService.upsertEntity[Address](profile.registrationId, index, address.normalise())
          } yield Redirect(routes.PartnerTelephoneNumberController.show(index))
        }
  }
}
