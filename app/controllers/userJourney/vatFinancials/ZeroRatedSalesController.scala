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
import forms.vatDetails.vatFinancials.ZeroRatedSalesForm
import models.ZeroRatedTurnoverEstimatePath
import models.view.vatFinancials.ZeroRatedSales
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class ZeroRatedSalesController @Inject()(ds: CommonPlayDependencies)
                                        (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {
  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[ZeroRatedSales].map { vm =>
      Ok(views.html.pages.zero_rated_sales(ZeroRatedSalesForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.zero_rated_sales(ZeroRatedSalesForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    ZeroRatedSalesForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.zero_rated_sales(formWithErrors)))
      }, {
        data: ZeroRatedSales => {
          s4LService.saveForm[ZeroRatedSales](data) flatMap { _ =>
            if (ZeroRatedSales.ZERO_RATED_SALES_NO == data.yesNo) {
              vrs.deleteElement(ZeroRatedTurnoverEstimatePath).map { _ =>
                Redirect(controllers.userJourney.vatFinancials.routes.VatChargeExpectancyController.show()) }
            } else {
              Future.successful(Redirect(controllers.userJourney.vatFinancials.routes.EstimateZeroRatedSalesController.show()))
            }
          }
        }
      })
  })

}
