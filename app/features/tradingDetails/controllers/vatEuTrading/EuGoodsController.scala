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

  case class EuGoods(yesNo: String)

  object EuGoods {

    val EU_GOODS_YES = "EU_GOODS_YES"
    val EU_GOODS_NO = "EU_GOODS_NO"

    val valid = (item: String) => List(EU_GOODS_YES, EU_GOODS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[EuGoods]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.euGoods,
      updateF = (c: EuGoods, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(euGoods = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[EuGoods] { (vs: VatScheme) =>
      vs.tradingDetails.map(td => EuGoods(if (td.euTrading.selection) EU_GOODS_YES else EU_GOODS_NO))
    }
  }
}

package controllers.vatTradingDetails.vatEuTrading {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatEuTrading.EuGoodsForm
  import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class EuGoodsController @Inject()(ds: CommonPlayDependencies,
                                    val keystoreConnector: KeystoreConnect,
                                    val authConnector: AuthConnector,
                                    implicit val s4LService: S4LService,
                                    implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val form = EuGoodsForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[EuGoods]().fold(form)(form.fill)
                .map(f => Ok(features.tradingDetails.views.html.vatEuTrading.eu_goods(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.tradingDetails.views.html.vatEuTrading.eu_goods(badForm)).pure,
                goodForm => save(goodForm).map(_ => goodForm.yesNo == EuGoods.EU_GOODS_NO).ifM(
                  save(ApplyEori(ApplyEori.APPLY_EORI_NO)).map(_ =>
                    controllers.vatFinancials.routes.EstimateVatTurnoverController.show()),
                  controllers.vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show().pure
                ).map(Redirect))
            }
          }
    }
  }
}

package forms.vatTradingDetails.vatEuTrading {

  import forms.FormValidation.textMapping
  import models.view.vatTradingDetails.vatEuTrading.EuGoods
  import play.api.data.Form
  import play.api.data.Forms._

  object EuGoodsForm {
    val RADIO_YES_NO: String = "euGoodsRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("euGoods").verifying(EuGoods.valid)
      )(EuGoods.apply)(EuGoods.unapply)
    )
  }
}
