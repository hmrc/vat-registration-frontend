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

package controllers.vatLodgingOfficer

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.vatTradingDetails.vatChoice.{routes => vatChoiceRoutes}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.OfficerContactDetailsForm
import models.view.vatLodgingOfficer.OfficerContactDetailsView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

class OfficerContactDetailsController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val form = OfficerContactDetailsForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[OfficerContactDetailsView]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatLodgingOfficer.officer_contact_details(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      copyGlobalErrorsToFields("email", "daytimePhone", "mobile")
        .andThen(form => BadRequest(views.html.pages.vatLodgingOfficer.officer_contact_details(form)).pure),
      data => save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show()))))

}
