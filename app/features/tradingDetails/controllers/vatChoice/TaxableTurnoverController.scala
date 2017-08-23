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

package models.view.vatTradingDetails.vatChoice {

  import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat}
  import play.api.libs.json.Json

  case class TaxableTurnover(yesNo: String)

  object TaxableTurnover {

    val TAXABLE_YES = "TAXABLE_YES"
    val TAXABLE_NO = "TAXABLE_NO"

    val valid = (item: String) => List(TAXABLE_YES, TAXABLE_NO).contains(item.toUpperCase)

    implicit val format = Json.format[TaxableTurnover]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.taxableTurnover,
      updateF = (c: TaxableTurnover, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(taxableTurnover = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[TaxableTurnover] { (vs: VatScheme) =>
      vs.tradingDetails.map(_.vatChoice.necessity).collect {
        case NECESSITY_VOLUNTARY => TaxableTurnover(TAXABLE_NO)
        case NECESSITY_OBLIGATORY => TaxableTurnover(TAXABLE_YES)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.Inject

  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatChoice.TaxableTurnoverForm
  import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
  import models.view.vatTradingDetails.vatChoice.TaxableTurnover.TAXABLE_YES
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
  import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration}
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, VatRegistrationService}

  class TaxableTurnoverController @Inject()(ds: CommonPlayDependencies)
                                           (implicit s4LService: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) {

    import cats.syntax.flatMap._

    val form = TaxableTurnoverForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[TaxableTurnover]().fold(form)(form.fill)
        .map(f => Ok(features.tradingDetails.views.html.vatChoice.taxable_turnover(f))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(features.tradingDetails.views.html.vatChoice.taxable_turnover(badForm)).pure,
        (data: TaxableTurnover) => save(data).map(_ => data.yesNo == TAXABLE_YES).ifM(
          for {
            _ <- save(VoluntaryRegistration(REGISTER_NO))
            _ <- save(StartDateView(COMPANY_REGISTRATION_DATE))
          } yield controllers.vatLodgingOfficer.routes.CompletionCapacityController.show(),
          controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show().pure
        ) map Redirect))
  }
}

package forms.vatTradingDetails.vatChoice {

  import forms.FormValidation.textMapping
  import models.view.vatTradingDetails.vatChoice.TaxableTurnover
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object TaxableTurnoverForm {
    val RADIO_YES_NO: String = "taxableTurnoverRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("taxable.turnover").verifying(TaxableTurnover.valid)
      )(TaxableTurnover.apply)(TaxableTurnover.unapply)
    )
  }
}
