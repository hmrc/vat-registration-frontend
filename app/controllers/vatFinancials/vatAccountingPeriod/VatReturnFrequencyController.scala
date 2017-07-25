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
import models.S4LVatSicAndCompliance.financeOnly
import models.{AccountingPeriodStartPath, S4LVatFinancials, S4LVatSicAndCompliance}
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.MONTHLY
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import play.api.mvc._
import services.{CommonService, S4LService, VatRegistrationService}

class VatReturnFrequencyController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService with FlatMapSyntax {

  val joinThreshold: Long = conf.getLong("thresholds.frs.joinThreshold").get

  val form = VatReturnFrequencyForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[VatReturnFrequency]().fold(form)(form.fill)
      .map(frm => Ok(views.html.pages.vatFinancials.vatAccountingPeriod.vat_return_frequency(frm))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vatAccountingPeriod.vat_return_frequency(badForm)).pure,
      view => save(view).map(_ => view.frequencyType == MONTHLY).ifM(
        ifTrue = for {
          container <- s4lContainer[S4LVatFinancials]()
          _ <- s4l.save(container.copy(accountingPeriod = None))
          voluntaryReg <- viewModel[VoluntaryRegistration]().fold(true)(_ == VoluntaryRegistration.yes)
        } yield if (voluntaryReg) {
            controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()
          } else {
            controllers.vatTradingDetails.vatChoice.routes.MandatoryStartDateController.show()
          },
        ifFalse = controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show().pure
      ).map(Redirect)))

}
