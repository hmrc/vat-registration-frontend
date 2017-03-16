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

package controllers.userJourney.vatFinancials

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.vatFinancials.VatReturnFrequencyForm
import models.view.vatFinancials.VatReturnFrequency
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class VatReturnFrequencyController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {
  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[VatReturnFrequency].map { vm =>
      Ok(views.html.pages.vat_return_frequency(VatReturnFrequencyForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.vat_return_frequency(VatReturnFrequencyForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VatReturnFrequencyForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vat_return_frequency(formWithErrors)))
      }, {
        data: VatReturnFrequency => {
          s4LService.saveForm[VatReturnFrequency](data) flatMap { _ =>
            if (VatReturnFrequency.MONTHLY == data.frequencyType) {
              vrs.deleteAccountingPeriodStart().map { _ =>
                Redirect(controllers.userJourney.routes.SummaryController.show()) }
            } else {
              Future.successful(Redirect(controllers.userJourney.vatFinancials.routes.AccountingPeriodController.show()))
            }
          }
        }
      })
  })
}
