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

import cats.data.OptionT
import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatChoice.OverThresholdFormFactory
import models.ModelKeys._
import models.MonthYearModel.FORMAT_DD_MMMM_Y
import models.external.IncorporationInfo
import models.view.vatTradingDetails.vatChoice.OverThresholdView
import play.api.mvc._
import services.{CommonService, IncorpInfoService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

class OverThresholdController @Inject()(formFactory: OverThresholdFormFactory, ds: CommonPlayDependencies)
                                       (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax with CommonService {
  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    for {
      dateOfIncorporation <- fetchDateOfIncorporation()
      form <- viewModel[OverThresholdView]().fold(formFactory.form(dateOfIncorporation))(formFactory.form(dateOfIncorporation).fill)
    } yield Ok(views.html.pages.vatTradingDetails.vatChoice.over_threshold(form, dateOfIncorporation.format(FORMAT_DD_MMMM_Y)))
  }
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    fetchDateOfIncorporation().flatMap(date =>
      formFactory.form(date).bindFromRequest().fold(badForm =>
        BadRequest(views.html.pages.vatTradingDetails.vatChoice.over_threshold(badForm, date.format(FORMAT_DD_MMMM_Y))).pure,
        data => save(data).map(_ => Redirect(controllers.vatTradingDetails.vatChoice.routes.ThresholdSummaryController.show()))
      )
    )
  }
  )

}
