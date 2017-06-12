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

package controllers.vatFinancials

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.VatChargeExpectancyForm
import models.view.vatFinancials.VatChargeExpectancy
import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_YES
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class VatChargeExpectancyController @Inject()(ds: CommonPlayDependencies)
                                             (implicit s4LService: S4LService,
                                              vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val form = VatChargeExpectancyForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[VatChargeExpectancy]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatFinancials.vat_charge_expectancy(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vat_charge_expectancy(badForm)).pure,
      view => save(view).map(_ => view.yesNo == VAT_CHARGE_YES).ifM(
        controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show().pure,
        save(VatReturnFrequency(VatReturnFrequency.QUARTERLY))
          .map(_ => controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show())
      ).map(Redirect)))

}
