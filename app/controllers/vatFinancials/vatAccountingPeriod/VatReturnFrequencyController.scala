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

package controllers.vatFinancials.vatAccountingPeriod

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.vatAccountingPeriod.VatReturnFrequencyForm
import models.AccountingPeriodStartPath
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.MONTHLY
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class VatReturnFrequencyController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val form = VatReturnFrequencyForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[VatReturnFrequency]().fold(form)(form.fill)
      .map(frm => Ok(views.html.pages.vatFinancials.vatAccountingPeriod.vat_return_frequency(frm))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vatAccountingPeriod.vat_return_frequency(badForm)).pure,
      view => save(view).map(_ => view.frequencyType == MONTHLY).ifM(
        for {
          _ <- vrs.deleteElement(AccountingPeriodStartPath)
          _ <- vrs.submitVatFinancials()
        } yield controllers.routes.SummaryController.show(),
        controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show().pure)
        .map(Redirect)))

}
