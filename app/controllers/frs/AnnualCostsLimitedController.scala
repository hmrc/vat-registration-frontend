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
import forms.frs.AnnualCostsLimitedFormFactory
import models.ElementPath.fromFrsAnnualCostsLimitedElementPaths
import models._
import models.view.frs.AnnualCostsLimitedView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class AnnualCostsLimitedController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val defaultForm = AnnualCostsLimitedFormFactory.form()

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      estimateVatTurnover <- vrs.getFlatRateSchemeThreshold()
      annualCostsLimitedForm <- viewModel[AnnualCostsLimitedView]().fold(defaultForm)(defaultForm.fill)
    } yield Ok(views.html.pages.frs.annual_costs_limited(annualCostsLimitedForm, estimateVatTurnover)))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    vrs.getFlatRateSchemeThreshold().flatMap(turnover =>
      AnnualCostsLimitedFormFactory.form(Seq(turnover)).bindFromRequest().fold(
        badForm => BadRequest(views.html.pages.frs.annual_costs_limited(badForm, turnover)).pure,
        view => save(view).map(_ => view.selection == AnnualCostsLimitedView.NO).ifM(
          ifTrue = controllers.frs.routes.ConfirmBusinessSectorController.show().pure,
          ifFalse = vrs.deleteElements(fromFrsAnnualCostsLimitedElementPaths).
            map(_ => controllers.frs.routes.RegisterForFrsController.show())
        ).map(Redirect))))

}
