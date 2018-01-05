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

package models.view.vatTradingDetails.vatEuTrading {

  import models.api._
  import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat}
  import play.api.libs.json.Json

  case class ApplyEori(yesNo: Boolean)

  object ApplyEori {

    val APPLY_EORI_YES = true
    val APPLY_EORI_NO = false

    implicit val format = Json.format[ApplyEori]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.applyEori,
      updateF = (c: ApplyEori, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(applyEori = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[ApplyEori] { vs: VatScheme =>
      vs.tradingDetails.flatMap(td => td.euTrading.eoriApplication).map(ApplyEori.apply)
    }

  }
}

package controllers.vatTradingDetails.vatEuTrading {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatEuTrading.ApplyEoriForm
  import models.view.vatTradingDetails.vatEuTrading.ApplyEori
  import play.api.data.Form
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class ApplyEoriController @Inject()(ds: CommonPlayDependencies,
                                      val keystoreConnector: KeystoreConnect,
                                      val authConnector: AuthConnector,
                                      implicit val s4l: S4LService,
                                      implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form: Form[ApplyEori] = ApplyEoriForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[ApplyEori]().fold(form)(form.fill)
                .map(f => Ok(features.tradingDetails.views.html.vatEuTrading.eori_apply(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.tradingDetails.views.html.vatEuTrading.eori_apply(badForm)).pure,
                goodForm => save(goodForm).map(_ =>
                  Redirect(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
                )
              )
            }
          }
    }
  }
}

package forms.vatTradingDetails.vatEuTrading {

  import forms.FormValidation.missingBooleanFieldMapping
  import models.view.vatTradingDetails.vatEuTrading.ApplyEori
  import play.api.data.Form
  import play.api.data.Forms._

  object ApplyEoriForm {
    val RADIO_YES_NO: String = "applyEoriRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("applyEori")
      )(ApplyEori.apply)(ApplyEori.unapply)
    )
  }
}
