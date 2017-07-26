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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatChoice.OverThresholdFormFactory
import models.view.vatTradingDetails.vatChoice.OverThresholdView
import play.api.data.Form
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class OverThresholdController @Inject()(overThresholdFormFactory: OverThresholdFormFactory, ds: CommonPlayDependencies)
                                       (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val presentationFormatter = DateTimeFormatter.ofPattern("dd MMMM y")
  val dateOfIncorporation = LocalDate.now().minusMonths(2) //fixed date until we can get the DOI from II
  val form: Form[OverThresholdView] = overThresholdFormFactory.form(dateOfIncorporation)

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[OverThresholdView]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatChoice.over_threshold((f), dateOfIncorporation.format(presentationFormatter)))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatTradingDetails.vatChoice.over_threshold(badForm, dateOfIncorporation.format(presentationFormatter))).pure,
      data => save(data).map(_ => Redirect(controllers.vatTradingDetails.vatChoice.routes.ThresholdSummaryController.show()))))

}
