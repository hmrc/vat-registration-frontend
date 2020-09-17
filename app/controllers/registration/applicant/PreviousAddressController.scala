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

import common.enums.AddressLookupJourneyIdentifier.addressThreeYearsOrLess
import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.BaseController
import forms.PreviousAddressForm
import javax.inject.{Inject, Singleton}
import models.view.PreviousAddressView
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import controllers.registration.applicant.{routes => applicantRoutes}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreviousAddressController @Inject()(mcc: MessagesControllerComponents,
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
        filledForm = applicant.previousAddress.fold(PreviousAddressForm.form)(PreviousAddressForm.form.fill)
      } yield Ok(views.html.previous_address(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      PreviousAddressForm.form.bindFromRequest.fold(
        badForm => Future.successful(BadRequest(views.html.previous_address(badForm))),
        data => if (!data.yesNo) {
          addressLookupService.getJourneyUrl(addressThreeYearsOrLess, applicantRoutes.PreviousAddressController.addressLookupCallback()) map Redirect
        } else {
          applicantDetailsService.saveApplicantDetails(data) map {
            _ => Redirect(controllers.routes.BusinessContactDetailsController.showPPOB())
          }
        }
      )
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        address <- addressLookupService.getAddressById(id)
        _ <- applicantDetailsService.saveApplicantDetails(PreviousAddressView(yesNo = false, Some(address)))
      } yield Redirect(controllers.routes.BusinessContactDetailsController.showPPOB())
  }

  def change: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      addressLookupService.getJourneyUrl(addressThreeYearsOrLess, applicantRoutes.PreviousAddressController.addressLookupCallback()) map Redirect
  }

}
