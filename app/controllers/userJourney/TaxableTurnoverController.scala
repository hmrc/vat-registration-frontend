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
import enums.CacheKeys
import forms.vatDetails.TaxableTurnoverForm
import models.view.{StartDate, TaxableTurnover, VoluntaryRegistration}
import models.view.StartDate.COMPANY_REGISTRATION_DATE
import models.view.VoluntaryRegistration.REGISTER_NO
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}



class TaxableTurnoverController @Inject()(s4LService: S4LService, vatRegistrationService: VatRegistrationService,
                                          ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    s4LService.fetchAndGet[TaxableTurnover](CacheKeys.TaxableTurnover.toString) flatMap {
      case Some(viewModel) => Future.successful(viewModel)
      case None => for {
        vatScheme <- vatRegistrationService.getVatScheme()
        viewModel = TaxableTurnover(vatScheme)
      } yield viewModel
    } map { viewModel =>
      val form = TaxableTurnoverForm.form.fill(viewModel)
      Ok(views.html.pages.taxable_turnover(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    TaxableTurnoverForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.taxable_turnover(formWithErrors)))
      }, {

        data: TaxableTurnover => {
          s4LService.saveForm[TaxableTurnover](CacheKeys.TaxableTurnover.toString, data) flatMap { _ =>
            if (TaxableTurnover.TAXABLE_YES == data.yesNo) {
              for {
                _ <- s4LService.saveForm[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString, VoluntaryRegistration(REGISTER_NO))
                _ <- s4LService.saveForm[StartDate](CacheKeys.StartDate.toString, StartDate.empty(COMPANY_REGISTRATION_DATE))
              } yield Redirect(controllers.userJourney.routes.MandatoryStartDateController.show())
            } else {
              Future.successful(Redirect(controllers.userJourney.routes.VoluntaryRegistrationController.show()))
            }
          }
        }
      })
  })

}
