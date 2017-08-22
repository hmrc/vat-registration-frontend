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
import forms.frs.AnnualCostsInclusiveForm
import models._
import models.view.frs.AnnualCostsInclusiveView.NO
import models.view.frs.{AnnualCostsInclusiveView, JoinFrsView}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class AnnualCostsInclusiveController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val PREVIOUS_QUESTION_THRESHOLD = 1000L
  val form = AnnualCostsInclusiveForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[AnnualCostsInclusiveView]().fold(form)(form.fill)
      .map(f => Ok(features.frs.views.html.annual_costs_inclusive(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(features.frs.views.html.annual_costs_inclusive(badForm)).pure,
      view => (if (view.selection == NO) {
        save(view).flatMap(_ =>
          getFlatRateSchemeThreshold().map {
            case n if n > PREVIOUS_QUESTION_THRESHOLD => controllers.frs.routes.AnnualCostsLimitedController.show()
            case _ => controllers.frs.routes.ConfirmBusinessSectorController.show()
          })
      } else {
        for {
          // save annualCostsInclusive and delete all later elements
          _ <- s4LService.save(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(view)))
        } yield controllers.frs.routes.RegisterForFrsController.show()
      }).map(Redirect)))

}
