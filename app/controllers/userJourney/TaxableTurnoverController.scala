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
import forms.vatDetails.TaxableTurnoverForm
import models.view.StartDate.COMPANY_REGISTRATION_DATE
import models.view.VoluntaryRegistration.REGISTER_NO
import models.view.{StartDate, TaxableTurnover, VoluntaryRegistration}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class TaxableTurnoverController @Inject()(ds: CommonPlayDependencies)
                                         (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[TaxableTurnover]() map { vm =>
      Ok(views.html.pages.taxable_turnover(TaxableTurnoverForm.form.fill(vm)))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    TaxableTurnoverForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.taxable_turnover(formWithErrors)))
      }, {
        data: TaxableTurnover => {
          s4LService.saveForm[TaxableTurnover](data) flatMap { _ =>
            if (TaxableTurnover.TAXABLE_YES == data.yesNo) {
              for {
                _ <- s4LService.saveForm[VoluntaryRegistration](VoluntaryRegistration(REGISTER_NO))
                _ <- s4LService.saveForm[StartDate](StartDate(COMPANY_REGISTRATION_DATE))
              } yield Redirect(controllers.userJourney.routes.MandatoryStartDateController.show())
            } else {
              Future.successful(Redirect(controllers.userJourney.routes.VoluntaryRegistrationController.show()))
            }
          }
        }
      })
  })

}
