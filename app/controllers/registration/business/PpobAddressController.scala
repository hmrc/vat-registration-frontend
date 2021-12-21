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

package controllers.registration.business

import common.enums.AddressLookupJourneyIdentifier
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController

import javax.inject.{Inject, Singleton}
import models.api.Address
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, BusinessContactService, SessionProfile, SessionService}

import scala.concurrent.ExecutionContext

@Singleton
class PpobAddressController @Inject()(val authConnector: AuthClientConnector,
                                      val sessionService: SessionService,
                                      val businessContactService: BusinessContactService,
                                      val addressLookupService: AddressLookupService)
                                     (implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def startJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => _ =>
        addressLookupService.getJourneyUrl(
          journeyId = AddressLookupJourneyIdentifier.businessActivities,
          continueUrl = routes.PpobAddressController.callback()
        ) map Redirect
  }

  def callback(id: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          address <- addressLookupService.getAddressById(id)
          _ <- businessContactService.updateBusinessContact[Address](address)
        } yield Redirect(controllers.registration.business.routes.BusinessContactDetailsController.show)
  }

}
