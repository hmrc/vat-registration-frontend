/*
 * Copyright 2020 HM Revenue & Customs
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

import common.enums.AddressLookupJourneyIdentifier.homeAddress
import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.BaseController
import deprecated.DeprecatedConstants
import forms.HomeAddressForm
import javax.inject.{Inject, Singleton}
import models.view.HomeAddressView
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import controllers.registration.applicant.{routes => applicantRoutes}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeAddressController @Inject()(mcc: MessagesControllerComponents,
                                      val authConnector: AuthConnector,
                                      val keystoreConnector: KeystoreConnector,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      val addressLookupService: AddressLookupService)
                                     (implicit val appConfig: FrontendAppConfig,
                                      ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        applicant <- applicantDetailsService.getApplicantDetails
        filledForm = applicant.homeAddress.fold(HomeAddressForm.form)(HomeAddressForm.form.fill)
      } yield Ok(views.html.applicant_home_address(filledForm, DeprecatedConstants.emptyAddressList))
  }


  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      applicantDetailsService.getApplicantDetails.flatMap { _ =>
        HomeAddressForm.form.bindFromRequest.fold(
          badForm =>
            Future.successful(BadRequest(views.html.applicant_home_address(badForm, DeprecatedConstants.emptyAddressList)))
          , _ =>
            addressLookupService.getJourneyUrl(homeAddress, applicantRoutes.HomeAddressController.addressLookupCallback()) map Redirect
        )
      }
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        address <- addressLookupService.getAddressById(id)
        _ <- applicantDetailsService.saveApplicantDetails(HomeAddressView(address.id, Some(address.normalise())))
      } yield Redirect(applicantRoutes.PreviousAddressController.show())
  }

}
