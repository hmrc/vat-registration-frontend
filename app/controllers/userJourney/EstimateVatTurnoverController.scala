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
import forms.vatDetails.EstimateVatTurnoverForm
import models.view.EstimateVatTurnover
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class EstimateVatTurnoverController @Inject()(s4LService: S4LService, vatRegistrationService: VatRegistrationService,
                                              ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    s4LService.fetchAndGet[EstimateVatTurnover](CacheKeys.VatTurnoverEstimate.toString) flatMap {
      case Some(viewModel) => Future.successful(viewModel)
      case None => for {
        vatScheme <- vatRegistrationService.getVatScheme()
        viewModel = EstimateVatTurnover(vatScheme)
      } yield viewModel
    } map { viewModel =>
      val form = EstimateVatTurnoverForm.form.fill(viewModel)
      Ok(views.html.pages.estimate_vat_turnover(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    EstimateVatTurnoverForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.estimate_vat_turnover(formWithErrors)))
      }, {
        data: EstimateVatTurnover => {
          s4LService.saveForm[EstimateVatTurnover](CacheKeys.VatTurnoverEstimate.toString, data) map { _ =>
            Redirect(controllers.userJourney.routes.StartDateController.show())
          }
        }
      })
  })

}
