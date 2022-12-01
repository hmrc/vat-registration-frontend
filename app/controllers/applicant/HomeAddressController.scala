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

import common.enums.AddressLookupJourneyIdentifier.{applicantAddress, homeAddress}
import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.applicant.{routes => applicantRoutes}
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService.CurrentAddress
import services._
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class HomeAddressController @Inject()(val authConnector: AuthConnector,
                                      val sessionService: SessionService,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      val addressLookupService: AddressLookupService,
                                      val vatRegistrationService: VatRegistrationService)
                                     (implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def redirectToAlf: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatRegistrationService.isTransactor.flatMap { isTransactor =>
          val journeyId = if (isTransactor) {
            applicantAddress
          } else {
            homeAddress
          }
          addressLookupService.getJourneyUrl(journeyId, applicantRoutes.HomeAddressController.addressLookupCallback()) map Redirect
        }
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          address <- addressLookupService.getAddressById(id)
          _ <- applicantDetailsService.saveApplicantDetails(CurrentAddress(address.normalise()))
        } yield Redirect(applicantRoutes.PreviousAddressController.show)
  }

}
