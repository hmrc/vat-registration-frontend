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

import javax.inject.Inject

package models.view.frs {

  import models._
  import models.api.{VatFlatRateScheme, VatScheme}
  import play.api.libs.json.Json

  case class AnnualCostsInclusiveView(selection: String)

  object AnnualCostsInclusiveView {

    val YES = "yes"
    val YES_WITHIN_12_MONTHS = "yesWithin12months"
    val NO = "no"

    val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

    implicit val format = Json.format[AnnualCostsInclusiveView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LFlatRateScheme) => group.annualCostsInclusive,
      updateF = (c: AnnualCostsInclusiveView, g: Option[S4LFlatRateScheme]) =>
        g.getOrElse(S4LFlatRateScheme()).copy(annualCostsInclusive = Some(c))
    )

    def from(vatFlatRateScheme: VatFlatRateScheme): Option[AnnualCostsInclusiveView] = {
      vatFlatRateScheme.annualCostsInclusive collect {
        case choice@(YES | YES_WITHIN_12_MONTHS | NO) => AnnualCostsInclusiveView(choice)
      }
    }

    implicit val modelTransformer = ApiModelTransformer[AnnualCostsInclusiveView] { vs: VatScheme =>
      vs.vatFlatRateScheme.flatMap(_.annualCostsInclusive).collect {
        case YES => AnnualCostsInclusiveView(YES)
        case YES_WITHIN_12_MONTHS => AnnualCostsInclusiveView(YES_WITHIN_12_MONTHS)
        case NO => AnnualCostsInclusiveView(NO)
      }
    }
  }
}

package controllers.frs {

  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.frs.AnnualCostsInclusiveForm
  import models.S4LFlatRateScheme
  import models.view.frs.AnnualCostsInclusiveView.NO
  import models.view.frs.{AnnualCostsInclusiveView, JoinFrsView}
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}

  class AnnualCostsInclusiveController @Inject()(ds: CommonPlayDependencies)
                                                (implicit s4LService: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    val PREVIOUS_QUESTION_THRESHOLD = 1000L
    val form = AnnualCostsInclusiveForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      withCurrentProfile { implicit profile =>
        ivPassedCheck {
          viewModel[AnnualCostsInclusiveView]().fold(form)(form.fill)
            .map(f => Ok(features.frs.views.html.annual_costs_inclusive(f)))
      }})

    def submit: Action[AnyContent] = authorised.async{
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
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
                }).map(Redirect))
            }
          }
    }
  }
}

package forms.frs {

  import forms.FormValidation.textMapping
  import models.view.frs.AnnualCostsInclusiveView
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object AnnualCostsInclusiveForm {

    val RADIO_INCLUSIVE: String = "annualCostsInclusiveRadio"

    val form = Form(
      mapping(
        RADIO_INCLUSIVE -> textMapping()("frs.costsInclusive")
          .verifying(AnnualCostsInclusiveView.valid)
      )(AnnualCostsInclusiveView.apply)(AnnualCostsInclusiveView.unapply)
    )
  }
}
