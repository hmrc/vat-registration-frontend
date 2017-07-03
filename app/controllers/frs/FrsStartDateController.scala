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

package controllers.frs

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.frs.FrsStartDateFormFactory
import models.view.frs.FrsStartDateView
import models.view.vatTradingDetails.vatChoice.StartDateView
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class FrsStartDateController @Inject()(frsStartDateFormFactory: FrsStartDateFormFactory, ds: CommonPlayDependencies)
                                      (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[FrsStartDateView]().getOrElse(FrsStartDateView())
      .map(f => Ok(views.html.pages.frs.frs_start_date(frsStartDateFormFactory.form().fill(f)))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    frsStartDateFormFactory.form().bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.frs.frs_start_date(badForm)).pure,
      view => if (view.dateType == FrsStartDateView.VAT_REGISTRATION_DATE) {
        val updateVatStartDate = setVatRegistrationDateToForm(view)
        updateVatStartDate.flatMap(frsStartDateView => saveForm(frsStartDateView))
      } else {
        saveForm(view)
      }))

  private def setVatRegistrationDateToForm(view: FrsStartDateView)
                                          (implicit headerCarrier: HeaderCarrier): Future[FrsStartDateView] =
    viewModel[StartDateView]().fold(view)(startDateView => view.copy(date = startDateView.date))

  private def saveForm(view: FrsStartDateView)(implicit headerCarrier: HeaderCarrier): Future[Result] =
    save(view).flatMap(_ =>
      vrs.submitVatFlatRateScheme().map(_ =>
        Redirect(controllers.routes.SummaryController.show())))

}
