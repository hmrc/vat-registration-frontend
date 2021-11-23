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

package controllers.registration.transactor

import common.enums.AddressLookupJourneyIdentifier.homeAddress
import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, SessionProfile, TransactorDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TransactorHomeAddressController @Inject()(val authConnector: AuthConnector,
                                                val keystoreConnector: KeystoreConnector,
                                                val transactorDetailsService: TransactorDetailsService,
                                                val addressLookupService: AddressLookupService)
                                               (implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def redirectToAlf: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      _ =>
        addressLookupService.getJourneyUrl(homeAddress, routes.TransactorHomeAddressController.addressLookupCallback()) map Redirect
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          address <- addressLookupService.getAddressById(id)
          _ <- transactorDetailsService.saveTransactorDetails(address)
        } yield Redirect(routes.TelephoneNumberController.show.url)
  }
}
