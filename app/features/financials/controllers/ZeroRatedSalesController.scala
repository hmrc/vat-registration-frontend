/*
 * Copyright 2018 HM Revenue & Customs
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

  import models.api.{VatFinancials, VatScheme}
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class ZeroRatedSales(yesNo: String)

  object ZeroRatedSales {

    val ZERO_RATED_SALES_YES = "ZERO_RATED_SALES_YES"
    val ZERO_RATED_SALES_NO = "ZERO_RATED_SALES_NO"

    val yes = ZeroRatedSales(ZERO_RATED_SALES_YES)
    val no = ZeroRatedSales(ZERO_RATED_SALES_NO)

    val valid = (item: String) => List(ZERO_RATED_SALES_YES, ZERO_RATED_SALES_NO).contains(item.toUpperCase)

    implicit val format: OFormat[ZeroRatedSales] = Json.format[ZeroRatedSales]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (_: S4LVatFinancials).zeroRatedTurnover,
      updateF = (c: ZeroRatedSales, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(zeroRatedTurnover = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[ZeroRatedSales] { (vs: VatScheme) =>
      vs.financials.map {
        case VatFinancials(_, Some(_)) => ZeroRatedSales(ZERO_RATED_SALES_YES)
        case VatFinancials(_, None) => ZeroRatedSales(ZERO_RATED_SALES_NO)
      }
    }
  }
}

package controllers.vatFinancials {

  import javax.inject.{Inject, Singleton}

  import cats.data.OptionT
  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnect
  import controllers.vatFinancials.{routes => financialRoutes}
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.ZeroRatedSalesForm
  import models.S4LVatFinancials
  import models.view.vatFinancials.ZeroRatedSales
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class ZeroRatedSalesController @Inject()(ds: CommonPlayDependencies,
                                           implicit val s4lService: S4LService,
                                           val keystoreConnector: KeystoreConnect,
                                           val authConnector: AuthConnector,
                                           implicit val vrs: RegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax with SessionProfile {

    val form = ZeroRatedSalesForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[ZeroRatedSales]().fold(form)(form.fill)
                .map(f => Ok(features.financials.views.html.zero_rated_sales(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.financials.views.html.zero_rated_sales(badForm)).pure,
                view => save(view).map(_ => view.yesNo == ZeroRatedSales.ZERO_RATED_SALES_YES).ifM(
                  ifTrue = financialRoutes.EstimateZeroRatedSalesController.show().pure,
                  // delete any previous zeroRatedTurnoverEstimate by updating the container with None
                  ifFalse =
                    OptionT(s4lService.fetchAndGet[S4LVatFinancials])
                      .semiflatMap(container => s4lService.save(container.copy(zeroRatedTurnoverEstimate = None))).value
                      .map(_ => features.returns.routes.ReturnsController.chargeExpectancyPage())
                ).map(Redirect)
              )
            }
          }
    }
  }
}

package forms.vatFinancials {

  import forms.FormValidation.textMapping
  import models.view.vatFinancials.ZeroRatedSales
  import play.api.data.Form
  import play.api.data.Forms._

  object ZeroRatedSalesForm {
    val RADIO_YES_NO: String = "zeroRatedSalesRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("zero.rated.sales").verifying(ZeroRatedSales.valid)
      )(ZeroRatedSales.apply)(ZeroRatedSales.unapply)
    )
  }
}
