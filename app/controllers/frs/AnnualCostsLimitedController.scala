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

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.frs.AnnualCostsLimitedFormFactory
import models.view.frs.AnnualCostsLimitedView
import models.view.vatFinancials.EstimateVatTurnover
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class AnnualCostsLimitedController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  val defaultForm = AnnualCostsLimitedFormFactory.form()
  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    for {
      estimateVatTurnover <- viewModel[EstimateVatTurnover]().fold(0L)(turnover => (turnover.vatTurnoverEstimate * 0.02).toLong)
      annualCostsLimitedForm <- viewModel[AnnualCostsLimitedView]().fold(defaultForm)(defaultForm.fill)
    } yield Ok(views.html.pages.frs.annual_costs_limited(annualCostsLimitedForm, estimateVatTurnover))
  }
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    val annualCostsLimitedForm = viewModel[EstimateVatTurnover]().
      fold(defaultForm) (turnover => AnnualCostsLimitedFormFactory.form(Seq((turnover.vatTurnoverEstimate * 0.02).toLong)))

      annualCostsLimitedForm.flatMap(
          form =>
            form.bindFromRequest().
              fold(
                    badForm => {
                      for {
                        estimateVatTurnover <- viewModel[EstimateVatTurnover]().fold(0L)(turnover => (turnover.vatTurnoverEstimate * 0.02).toLong)
                      } yield BadRequest(views.html.pages.frs.annual_costs_limited(badForm, estimateVatTurnover))
                    }
                    ,
                    goodForm => save(goodForm).map(_ =>
                      Redirect(if (goodForm.selection == AnnualCostsLimitedView.NO) {
                        controllers.frs.routes.RegisterForFrsController.show()
                      } else {
                        controllers.frs.routes.RegisterForFrsController.show()
                      })
                    )
                )
            )
        }
)

}
