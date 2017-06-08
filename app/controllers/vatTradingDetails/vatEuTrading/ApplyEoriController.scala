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

package controllers.vatTradingDetails.vatEuTrading

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatEuTrading.ApplyEoriForm
import models.view.vatTradingDetails.vatEuTrading.ApplyEori
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class ApplyEoriController @Inject()(ds: CommonPlayDependencies)
                                   (implicit s4l: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  val form: Form[ApplyEori] = ApplyEoriForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[ApplyEori]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatEuTrading.eori_apply(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatTradingDetails.vatEuTrading.eori_apply(badForm)).pure,
      goodForm => save(goodForm).flatMap(_ => vrs.submitTradingDetails().map(_ =>
        Redirect(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())))))

}
