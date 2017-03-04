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

package controllers.userJourney

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.VatChargeExpectancyForm
import models.ApiModelTransformer
import models.view._
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class VatChargeExpectancyController @Inject()(s4LService: S4LService, vatRegistrationService: VatRegistrationService,
                                              ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    s4LService.fetchAndGet[VatChargeExpectancy]() flatMap {
      case Some(viewModel) => Future.successful(viewModel)
      case None => vatRegistrationService.getVatScheme() map ApiModelTransformer[VatChargeExpectancy].toViewModel
    } map { viewModel =>
      val form = VatChargeExpectancyForm.form.fill(viewModel)
      Ok(views.html.pages.vat_charge_expectancy(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VatChargeExpectancyForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vat_charge_expectancy(formWithErrors)))
      }, {
        data: VatChargeExpectancy => {
          s4LService.saveForm[VatChargeExpectancy](data) flatMap { _ =>
            if (VatChargeExpectancy.VAT_CHARGE_NO == data.yesNo) {
              s4LService.saveForm[VatReturnFrequency](VatReturnFrequency(VatReturnFrequency.QUARTERLY))
                .map { _ => Redirect(controllers.userJourney.routes.AccountingPeriodController.show()) }
            } else {
              Future.successful(Redirect(controllers.userJourney.routes.VatReturnFrequencyController.show()))
            }
          }
        }
      })
  })

}
