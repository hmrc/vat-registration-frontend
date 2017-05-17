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

package controllers.vatTradingDetails

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.TradingNameForm
import models.view.vatTradingDetails.TradingNameView
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class TradingNameController @Inject()(ds: CommonPlayDependencies)
                                     (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  val form = TradingNameForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[TradingNameView].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.trading_name(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatTradingDetails.trading_name(badForm)).pure
      ,
      (data: TradingNameView) => s4LService.saveForm[TradingNameView](data) map { _ =>
        Redirect(controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())
      }
    )
  })

}
