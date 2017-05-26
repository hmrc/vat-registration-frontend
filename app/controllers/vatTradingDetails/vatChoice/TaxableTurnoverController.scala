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

package controllers.vatTradingDetails.vatChoice

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatChoice.TaxableTurnoverForm
import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
import models.view.vatTradingDetails.vatChoice.TaxableTurnover.TAXABLE_YES
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

class TaxableTurnoverController @Inject()(ds: CommonPlayDependencies)
                                         (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.syntax.flatMap._

  val form = TaxableTurnoverForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel2[TaxableTurnover].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatChoice.taxable_turnover(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatTradingDetails.vatChoice.taxable_turnover(badForm)).pure,
      (data: TaxableTurnover) => s4LService.save(data).map(_ => data.yesNo == TAXABLE_YES).ifM(
        for {
          _ <- s4LService.save(VoluntaryRegistration(REGISTER_NO))
          _ <- s4LService.save(StartDateView(COMPANY_REGISTRATION_DATE))
        } yield controllers.vatLodgingOfficer.routes.CompletionCapacityController.show(),
        controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show().pure
      ) map Redirect))

}
