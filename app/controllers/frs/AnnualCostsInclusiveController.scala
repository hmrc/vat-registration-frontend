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

package controllers.frs

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.frs.AnnualCostsInclusiveForm
import models.view.frs.AnnualCostsInclusiveView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class AnnualCostsInclusiveController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.syntax.flatMap._

  val form = AnnualCostsInclusiveForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[AnnualCostsInclusiveView]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.frs.annual_costs_inclusive(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.frs.annual_costs_inclusive(badForm)).pure,
      goodForm => (goodForm.selection == AnnualCostsInclusiveView.NO).pure.ifM(
        s4LService.clear().flatMap(_ => vrs.deleteVatScheme()).map(_ => controllers.routes.WelcomeController.show()),
        save(goodForm).map(_ => controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
      ).map(Redirect)))

}
