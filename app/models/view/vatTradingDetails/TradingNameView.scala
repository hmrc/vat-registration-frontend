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

package models.view.vatTradingDetails

import models.api.{TradingName, VatEuTrading, VatScheme, VatTradingDetails}
import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class TradingNameView(
                            yesNo: String,
                            tradingName: Option[String] = None
                          )

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
