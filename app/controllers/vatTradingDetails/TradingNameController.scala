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

import scala.concurrent.Future

class TradingNameController @Inject()(ds: CommonPlayDependencies)
                                     (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[TradingNameView].map { vm =>
      Ok(views.html.pages.vatTradingDetails.trading_name(TradingNameForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.vatTradingDetails.trading_name(TradingNameForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    TradingNameForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vatTradingDetails.trading_name(formWithErrors)))
      }, {
        data: TradingNameView => {
          //TODO this can be done in the Form definition using the `ignoring` mapping
          //blank out trading name (if present) when 'No' selected
          val toSave = if (data.yesNo == TradingNameView.TRADING_NAME_NO) data.copy(tradingName = None) else data
          s4LService.saveForm[TradingNameView](toSave) map { _ =>
            Redirect(controllers.vatContact.routes.BusinessContactDetailsController.show())
          }
        }
      })
  })

}
