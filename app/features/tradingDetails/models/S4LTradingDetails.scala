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

package features.tradingDetails.models

import models.S4LKey
import play.api.libs.json.Json

case class TradingNameView( yesNo : String,
                            tradingName: Option[String] = None)
object TradingNameView {
  val TRADING_NAME_YES = "TRADING_NAME_YES"
  val TRADING_NAME_NO = "TRADING_NAME_NO"

  implicit val format = Json.format[TradingNameView]
}

case class EuGoodsView(yesNo: Boolean)
object EuGoodsView {
  val EU_GOODS_YES = true
  val EU_GOODS_NO = false

  implicit val format = Json.format[EuGoodsView]
}

case class ApplyEoriView(yesNo: Boolean)
object ApplyEoriView {
  val APPLY_EORI_YES = true
  val APPLY_EORI_NO = false

  implicit val format = Json.format[ApplyEoriView]
}

case class S4LTradingDetails(tradingNameView: Option[TradingNameView] = None,
                             euGoodsView: Option[EuGoodsView] = None,
                             applyEoriView: Option[ApplyEoriView] = None)
object S4LTradingDetails {
  implicit val s4lkey: S4LKey[S4LTradingDetails] = S4LKey("TradingDetails")
  implicit val format = Json.format[S4LTradingDetails]
}
