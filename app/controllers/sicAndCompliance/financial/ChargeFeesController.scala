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

package controllers.sicAndCompliance.financial

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import controllers.sicAndCompliance.ComplianceExitController
import forms.sicAndCompliance.financial.ChargeFeesForm
import models.view.sicAndCompliance.financial.ChargeFees
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, RegistrationService, S4LService}


class ChargeFeesController @Inject()(ds: CommonPlayDependencies)
                                    (implicit s4LService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  val form: Form[ChargeFees] = ChargeFeesForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[ChargeFees]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.charge_fees(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.charge_fees(badForm)).pure,
      view => save(view).map(_ =>
        Redirect(controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show()))))

}


