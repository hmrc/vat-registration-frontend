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

package models.api {


  import play.api.libs.json._

  case class VatTradingDetails(
                                tradingName: TradingName,
                                euTrading: VatEuTrading
                              ) {

  }

  object VatTradingDetails {
    implicit val format: OFormat[VatTradingDetails] = Json.format[VatTradingDetails]
  }
}

package models {

  import common.ErrorUtil.fail
  import models.api._
  import models.view.vatTradingDetails.TradingNameView
  import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
  import models.view.vatTradingDetails.vatEuTrading.EuGoods.EU_GOODS_YES
  import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
  import play.api.libs.json.{Json, OFormat}

  final case class S4LTradingDetails
  (
    tradingName: Option[TradingNameView] = None,
    euGoods: Option[EuGoods] = None,
    applyEori: Option[ApplyEori] = None
  )

  object S4LTradingDetails {
    implicit val format: OFormat[S4LTradingDetails] = Json.format[S4LTradingDetails]
    implicit val tradingDetails: S4LKey[S4LTradingDetails] = S4LKey("VatTradingDetails")

    implicit val modelT = new S4LModelTransformer[S4LTradingDetails] {
      // map VatScheme to VatTradingDetails
      override def toS4LModel(vs: VatScheme): S4LTradingDetails =
        S4LTradingDetails(
          tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
          euGoods = ApiModelTransformer[EuGoods].toViewModel(vs),
          applyEori = ApiModelTransformer[ApplyEori].toViewModel(vs)
        )
    }

    def error = throw fail("VatTradingDetails")

    implicit val apiT = new S4LApiTransformer[S4LTradingDetails, VatTradingDetails] {
      // map S4LTradingDetails to VatTradingDetails
      override def toApi(c: S4LTradingDetails): VatTradingDetails =
        VatTradingDetails(
          tradingName = c.tradingName.map(tnv =>
            TradingName(tnv.yesNo == TRADING_NAME_YES, tnv.tradingName)).getOrElse(error),

          euTrading = VatEuTrading(
            selection = c.euGoods.map(_.yesNo == EU_GOODS_YES).getOrElse(error),
            eoriApplication = c.applyEori.map(_.yesNo)
          )
        )
    }
  }

}
