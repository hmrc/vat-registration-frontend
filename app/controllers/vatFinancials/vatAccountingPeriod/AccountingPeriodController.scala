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

import controllers.vatFinancials.EstimateVatTurnover.lastKnownValueKey
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.vatAccountingPeriod.AccountingPeriodForm
import models.VatFlatRateSchemePath
import models.view.vatFinancials.EstimateVatTurnover
import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, VatRegistrationService}


class AccountingPeriodController @Inject()(ds: CommonPlayDependencies)
                                          (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds)  with CommonService {

  val joinThreshold: Long = conf.getLong("thresholds.frs.joinThreshold").get

  val form = AccountingPeriodForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[AccountingPeriod]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatFinancials.vatAccountingPeriod.accounting_period(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vatAccountingPeriod.accounting_period(badForm)).pure,
      data => for {
        originalTurnover <- keystoreConnector.fetchAndGet[Long](lastKnownValueKey)
        _ <- save(data)
        _ <- vrs.submitVatFinancials()
        turnover <- viewModel[EstimateVatTurnover]().fold[Long](0)(_.vatTurnoverEstimate)
        _ <- vrs.conditionalDeleteElement(VatFlatRateSchemePath, originalTurnover.getOrElse(0) != turnover)
      } yield Redirect(if (turnover > joinThreshold) {
        controllers.routes.SummaryController.show()
      } else {
        controllers.frs.routes.JoinFrsController.show()
      })))

}