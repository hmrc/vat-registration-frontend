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

import java.time.LocalDate
import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.frs.FrsStartDateFormFactory
import models.view.frs.FrsStartDateView
import models.view.vatTradingDetails.vatChoice.StartDateView
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class FrsStartDateController @Inject()(frsStartDateFormFactory: FrsStartDateFormFactory, ds: CommonPlayDependencies)
                                      (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[FrsStartDateView]().getOrElse(FrsStartDateView())
      .map(f => Ok(views.html.pages.frs.frs_start_date(frsStartDateFormFactory.form().fill(f)))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[StartDateView]().getOrElse(StartDateView()).flatMap(a =>
      frsStartDateFormFactory.form(Some(a.date.getOrElse(LocalDate.now))).bindFromRequest().fold(
        badForm => BadRequest(views.html.pages.frs.frs_start_date(badForm)).pure,
        goodForm => save(goodForm).map(_ =>
          Redirect(controllers.vatTradingDetails.routes.TradingNameController.show())))))

}
