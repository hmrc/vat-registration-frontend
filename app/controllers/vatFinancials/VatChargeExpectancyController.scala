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

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.VatChargeExpectancyForm
import models.view.vatFinancials.VatChargeExpectancy
import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_NO
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.ExecutionContext.Implicits.global


class VatChargeExpectancyController @Inject()(ds: CommonPlayDependencies)
                                             (implicit s4LService: S4LService,
                                              vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  val form = VatChargeExpectancyForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel2[VatChargeExpectancy].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatFinancials.vat_charge_expectancy(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vat_charge_expectancy(badForm)).pure,
      (data: VatChargeExpectancy) =>
        s4LService.save(data).map(_ => VAT_CHARGE_NO == data.yesNo).ifM(
          s4LService.save(VatReturnFrequency(VatReturnFrequency.QUARTERLY))
            .map(_ => controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show())
          ,
          controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show().pure
        ) map Redirect
    ))

}
