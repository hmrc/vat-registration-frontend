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

package models.view.vatTradingDetails {

  import models.api.{TradingName, VatEuTrading, VatScheme, VatTradingDetails}
  import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat}
  import play.api.libs.json.Json

  case class TradingNameView(yesNo: String,
                             tradingName: Option[String] = None)

  object TradingNameView {

    val TRADING_NAME_YES = "TRADING_NAME_YES"
    val TRADING_NAME_NO = "TRADING_NAME_NO"

    val valid = (item: String) => List(TRADING_NAME_YES, TRADING_NAME_NO).contains(item.toUpperCase)

    implicit val format = Json.format[TradingNameView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.tradingName,
      updateF = (c: TradingNameView, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(tradingName = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
      vs.tradingDetails.map {
        case VatTradingDetails(_, TradingName(_, Some(tn)), VatEuTrading(_, _)) =>
          TradingNameView(TRADING_NAME_YES, Some(tn))
        case _ => TradingNameView(TRADING_NAME_NO)
      }
    }
  }
}

package controllers.vatTradingDetails {

  import javax.inject.Inject

  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.TradingNameForm
  import models.view.vatTradingDetails.TradingNameView
  import play.api.mvc._
  import services.{S4LService, SessionProfile, VatRegistrationService}

  class TradingNameController @Inject()(ds: CommonPlayDependencies)
                                       (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService)
    extends VatRegistrationController(ds) with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    val form = TradingNameForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[TradingNameView]().fold(form)(form.fill)
              .map(f => Ok(features.tradingDetails.views.html.trading_name(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.tradingDetails.views.html.trading_name(badForm)).pure,
              goodForm => save(goodForm).map(_ =>
                Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())))
          }
    }
  }
}

package forms.vatTradingDetails {

  import forms.FormValidation._
  import models.view.vatTradingDetails.TradingNameView
  import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
  import play.api.data.Form
  import play.api.data.Forms._
  import uk.gov.voa.play.form.ConditionalMappings._

  object TradingNameForm {

    val RADIO_YES_NO: String = "tradingNameRadio"
    val INPUT_TRADING_NAME: String = "tradingName"

    val TRADING_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$""".r

    implicit val errorCode: ErrorCode = INPUT_TRADING_NAME

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()(RADIO_YES_NO).verifying(TradingNameView.valid),
        INPUT_TRADING_NAME -> mandatoryIf(
          isEqual(RADIO_YES_NO, TRADING_NAME_YES),
          text.verifying(nonEmptyValidText(TRADING_NAME_REGEX)))
      )(TradingNameView.apply)(TradingNameView.unapply)
    )
  }
}
