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

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat}
  import play.api.libs.json.Json

  case class VoluntaryRegistrationReason(reason: String)

  object VoluntaryRegistrationReason {

    val SELLS = "COMPANY_ALREADY_SELLS_TAXABLE_GOODS_OR_SERVICES"
    val INTENDS_TO_SELL = "COMPANY_INTENDS_TO_SELLS_TAXABLE_GOODS_OR_SERVICES_IN_THE_FUTURE"
    val NEITHER = "NEITHER"

    //for convenience
    val sells = VoluntaryRegistrationReason(SELLS)
    val intendsToSell = VoluntaryRegistrationReason(INTENDS_TO_SELL)
    val neither = VoluntaryRegistrationReason(NEITHER)

    val valid: (String) => Boolean = List(SELLS, INTENDS_TO_SELL, NEITHER).contains

    implicit val format = Json.format[VoluntaryRegistrationReason]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.voluntaryRegistrationReason,
      updateF = (c: VoluntaryRegistrationReason, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(voluntaryRegistrationReason = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
      vs.tradingDetails.flatMap(_.vatChoice.reason).collect {
        case SELLS => sells
        case INTENDS_TO_SELL => intendsToSell
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.Inject

  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatChoice.VoluntaryRegistrationReasonForm
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
  import play.api.mvc._
  import services.{S4LService, SessionProfile, VatRegistrationService}

  class VoluntaryRegistrationReasonController @Inject()(ds: CommonPlayDependencies)
                                                       (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    val form = VoluntaryRegistrationReasonForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[VoluntaryRegistrationReason]().fold(form)(form.fill)
              .map(f => Ok(features.tradingDetails.views.html.vatChoice.voluntary_registration_reason(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.tradingDetails.views.html.vatChoice.voluntary_registration_reason(badForm)).pure,
              goodForm => (goodForm.reason == VoluntaryRegistrationReason.NEITHER).pure.ifM(
                s4l.clear().flatMap(_ => vrs.deleteVatScheme()).map(_ => controllers.routes.WelcomeController.show()),
                save(goodForm).map(_ => controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
              ).map(Redirect))
          }
    }
  }
}

package forms.vatTradingDetails.vatChoice {

  import forms.FormValidation.textMapping
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object VoluntaryRegistrationReasonForm {

    val RADIO_REASON: String = "voluntaryRegistrationReasonRadio"

    val form = Form(
      mapping(
        RADIO_REASON -> textMapping()("voluntary.registration.reason")
          .verifying(VoluntaryRegistrationReason.valid)
      )(VoluntaryRegistrationReason.apply)(VoluntaryRegistrationReason.unapply)
    )
  }
}
