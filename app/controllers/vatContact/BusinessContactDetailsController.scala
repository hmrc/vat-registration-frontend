/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import connectors.KeystoreConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatContact.BusinessContactDetailsForm
import models.view.vatContact.BusinessContactDetails
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class BusinessContactDetailsController @Inject()(ds: CommonPlayDependencies,
                                                 val keystoreConnector: KeystoreConnect,
                                                 val authConnector: AuthConnector,
                                                 implicit val vrs: RegistrationService,
                                                 implicit val s4l: S4LService) extends VatRegistrationController(ds) with SessionProfile {

  val form = BusinessContactDetailsForm.form

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            viewModel[BusinessContactDetails]().fold(form)(form.fill)
              .map(f => Ok(views.html.pages.vatContact.business_contact_details(f)))
          }
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            form.bindFromRequest().fold(
              copyGlobalErrorsToFields("daytimePhone", "mobile")
                .andThen(form => BadRequest(views.html.pages.vatContact.business_contact_details(form)).pure),
              contactDetails => for {
                _ <- save(contactDetails)
                _ <- vrs.submitVatContact
              } yield Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show()))
          }
        }
  }

}
