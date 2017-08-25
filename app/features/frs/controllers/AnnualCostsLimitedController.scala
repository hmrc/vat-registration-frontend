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

package models.view.frs {

  import models._
  import models.api.{VatFlatRateScheme, VatScheme}
  import play.api.libs.json.Json

  case class AnnualCostsLimitedView(selection: String)

  object AnnualCostsLimitedView {

    val YES = "yes"
    val YES_WITHIN_12_MONTHS = "yesWithin12months"
    val NO = "no"

    val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

    implicit val format = Json.format[AnnualCostsLimitedView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LFlatRateScheme) => group.annualCostsLimited,
      updateF = (c: AnnualCostsLimitedView, g: Option[S4LFlatRateScheme]) =>
        g.getOrElse(S4LFlatRateScheme()).copy(annualCostsLimited = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[AnnualCostsLimitedView] { vs: VatScheme =>
      vs.vatFlatRateScheme.flatMap(_.annualCostsLimited).collect {
        case YES => AnnualCostsLimitedView(YES)
        case YES_WITHIN_12_MONTHS => AnnualCostsLimitedView(YES_WITHIN_12_MONTHS)
        case NO => AnnualCostsLimitedView(NO)
      }
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.frs.AnnualCostsLimitedFormFactory
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
        estimateVatTurnover <- getFlatRateSchemeThreshold()
        annualCostsLimitedForm <- viewModel[AnnualCostsLimitedView]().fold(defaultForm)(defaultForm.fill)
      } yield Ok(features.frs.views.html.annual_costs_limited(annualCostsLimitedForm, estimateVatTurnover)))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      getFlatRateSchemeThreshold().flatMap(turnover =>
        AnnualCostsLimitedFormFactory.form(Seq(turnover)).bindFromRequest().fold(
          badForm => BadRequest(features.frs.views.html.annual_costs_limited(badForm, turnover)).pure,
          view => (if (view.selection == AnnualCostsLimitedView.NO) {
            save(view).map(_ => controllers.frs.routes.ConfirmBusinessSectorController.show())
          } else {
            for {
            // save this view and delete later elements
              frs <- s4lContainer[S4LFlatRateScheme]()
              _ <- s4LService.save(frs.copy(
                annualCostsLimited = Some(view),
                frsStartDate = None,
                categoryOfBusiness = None
              ))
            } yield controllers.frs.routes.RegisterForFrsController.show()
          }).map(Redirect))))

  }
}

package forms.frs {

  import forms.FormValidation.textMappingWithMessageArgs
  import models.view.frs.AnnualCostsLimitedView
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object AnnualCostsLimitedFormFactory {

    val RADIO_COST_LIMITED: String = "annualCostsLimitedRadio"

    def form(msgArgs: Seq[Any] = Seq()): Form[AnnualCostsLimitedView] = {
      Form(mapping(
        RADIO_COST_LIMITED -> textMappingWithMessageArgs()(msgArgs)("frs.costsLimited").verifying(AnnualCostsLimitedView.valid)
      )(AnnualCostsLimitedView.apply)(AnnualCostsLimitedView.unapply))
    }
  }
}
