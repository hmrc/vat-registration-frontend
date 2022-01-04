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

package controllers.registration.applicant

import common.enums.AddressLookupJourneyIdentifier.addressThreeYearsOrLess
import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import forms.PreviousAddressForm
import models.api.{NETP, NonUkNonEstablished}
import models.view.PreviousAddressView
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, ApplicantDetailsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.previous_address

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreviousAddressController @Inject()(val authConnector: AuthConnector,
                                          val sessionService: SessionService,
                                          val applicantDetailsService: ApplicantDetailsService,
                                          val addressLookupService: AddressLookupService,
                                          val vatRegistrationService: VatRegistrationService,
                                          previousAddressPage: previous_address
                                         )(implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.previousAddress.fold(PreviousAddressForm.form)(PreviousAddressForm.form.fill)
        } yield
          Ok(previousAddressPage(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        PreviousAddressForm.form.bindFromRequest.fold(
          badForm =>
            Future.successful(BadRequest(previousAddressPage(badForm))),
          data =>
            if (data.yesNo) {
              applicantDetailsService.saveApplicantDetails(data) map {
                _ => Redirect(routes.CaptureEmailAddressController.show)
              }
            } else {
              vatRegistrationService.partyType flatMap {
                case NETP | NonUkNonEstablished =>
                  Future.successful(Redirect(routes.InternationalPreviousAddressController.show))
                case _ =>
                  addressLookupService.getJourneyUrl(
                    addressThreeYearsOrLess,
                    applicantRoutes.PreviousAddressController.addressLookupCallback()
                  ) map Redirect
              }

            }
        )
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          address <- addressLookupService.getAddressById(id)
          _ <- applicantDetailsService.saveApplicantDetails(PreviousAddressView(yesNo = false, Some(address)))
        } yield Redirect(routes.CaptureEmailAddressController.show)
  }

  def change: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      _ =>
        addressLookupService.getJourneyUrl(addressThreeYearsOrLess, applicantRoutes.PreviousAddressController.addressLookupCallback()) map Redirect
  }

}
