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

package models.view.vatFinancials {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class EstimateZeroRatedSales(zeroRatedTurnoverEstimate: Long)

  object EstimateZeroRatedSales {

    implicit val format: OFormat[EstimateZeroRatedSales] = Json.format[EstimateZeroRatedSales]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.zeroRatedTurnoverEstimate,
      updateF = (c: EstimateZeroRatedSales, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(zeroRatedTurnoverEstimate = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[EstimateZeroRatedSales] { (vs: VatScheme) =>
      vs.financials.map(_.zeroRatedTurnoverEstimate).collect {
        case Some(sales) => EstimateZeroRatedSales(sales)
      }
    }
  }
}

package controllers.vatFinancials {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.EstimateZeroRatedSalesForm
  import models.view.vatFinancials.EstimateZeroRatedSales
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class EstimateZeroRatedSalesController @Inject()(ds: CommonPlayDependencies,
                                                   val keystoreConnector: KeystoreConnect,
                                                   val authConnector: AuthConnector,
                                                   implicit val s4LService: S4LService,
                                                   implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form = EstimateZeroRatedSalesForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[EstimateZeroRatedSales]().fold(form)(form.fill)
                .map(f => Ok(features.financials.views.html.estimate_zero_rated_sales(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.financials.views.html.estimate_zero_rated_sales(badForm)).pure,
                view => save(view) map (_ => Redirect(controllers.vatFinancials.routes.VatChargeExpectancyController.show()))
              )
            }
          }
    }
  }
}

package forms.vatFinancials {

  import forms.FormValidation._
  import models.view.vatFinancials.EstimateZeroRatedSales
  import play.api.data.Form
  import play.api.data.Forms._

  object EstimateZeroRatedSalesForm {

    val ZERO_RATED_SALES_ESTIMATE: String = "zeroRatedTurnoverEstimate"

    implicit val errorCode: ErrorCode = "estimate.zero.rated.sales"

    val form = Form(
      mapping(
        ZERO_RATED_SALES_ESTIMATE -> text.verifying(mandatoryNumericText).
          transform(taxEstimateTextToLong, longToText).verifying(boundedLong)
      )(EstimateZeroRatedSales.apply)(EstimateZeroRatedSales.unapply)
    )
  }
}
