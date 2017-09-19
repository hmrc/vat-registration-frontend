/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.vatContact

import javax.inject.Inject

import connectors.KeystoreConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatContact.BusinessContactDetailsForm
import models.view.vatContact.BusinessContactDetails
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile, VatRegistrationService}

class BusinessContactDetailsController @Inject()(ds: CommonPlayDependencies)
                                                (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with SessionProfile {

  val form = BusinessContactDetailsForm.form
  val keystoreConnector: KeystoreConnector = KeystoreConnector

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          viewModel[BusinessContactDetails]().fold(form)(form.fill)
            .map(f => Ok(views.html.pages.vatContact.business_contact_details(f)))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          form.bindFromRequest().fold(
            copyGlobalErrorsToFields("daytimePhone", "mobile")
              .andThen(form => BadRequest(views.html.pages.vatContact.business_contact_details(form)).pure),
            contactDetails => for {
              _ <- save(contactDetails)
              _ <- vrs.submitVatContact()
            } yield Redirect(controllers.vatTradingDetails.routes.TradingNameController.show()))
        }
  }

}
