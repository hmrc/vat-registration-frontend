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

package models.view.vatTradingDetails.vatChoice {

  import models.api.VatEligibilityChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatEligibilityChoice, ViewModelFormat}
  import play.api.libs.json.Json

  case class TaxableTurnover(yesNo: String)

  object TaxableTurnover {

    val TAXABLE_YES = "TAXABLE_YES"
    val TAXABLE_NO = "TAXABLE_NO"

    val valid = (item: String) => List(TAXABLE_YES, TAXABLE_NO).contains(item.toUpperCase)

    implicit val format = Json.format[TaxableTurnover]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatEligibilityChoice) => group.taxableTurnover,
      updateF = (c: TaxableTurnover, g: Option[S4LVatEligibilityChoice]) =>
        g.getOrElse(S4LVatEligibilityChoice()).copy(taxableTurnover = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[TaxableTurnover] { (vs: VatScheme) =>
      vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice.map(_.necessity)).collect {
        case NECESSITY_VOLUNTARY => TaxableTurnover(TAXABLE_NO)
        case NECESSITY_OBLIGATORY => TaxableTurnover(TAXABLE_YES)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatChoice.TaxableTurnoverForm
  import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
  import models.view.vatTradingDetails.vatChoice.TaxableTurnover.TAXABLE_YES
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
  import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration}
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class TaxableTurnoverController @Inject()(ds: CommonPlayDependencies,
                                            val keystoreConnector: KeystoreConnect,
                                            val authConnector: AuthConnector,
                                            implicit val s4LService: S4LService,
                                            implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val form = TaxableTurnoverForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[TaxableTurnover]().fold(form)(form.fill)
              .map(f => Ok(features.tradingDetails.views.html.vatChoice.taxable_turnover(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.tradingDetails.views.html.vatChoice.taxable_turnover(badForm)).pure,
              (data: TaxableTurnover) => save(data).map(_ => data.yesNo == TAXABLE_YES).ifM(
                for {
                  _ <- save(VoluntaryRegistration(REGISTER_NO))
                  _ <- save(StartDateView(COMPANY_REGISTRATION_DATE))
                } yield features.officer.controllers.routes.OfficerController.showCompletionCapacity(),
                controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show().pure
              ) map Redirect)
          }
    }
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
