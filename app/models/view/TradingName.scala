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

package models.view

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatScheme, VatTradingDetails}
import play.api.libs.json.Json

case class TradingName(yesNo: String,
                       tradingName: Option[String])
  extends ViewModelTransformer[VatTradingDetails] {

  override def toString: String = tradingName.getOrElse("")

  // Upserts (selectively converts) a View model object to its API model counterpart
  override def toApi(vatTradingDetails: VatTradingDetails): VatTradingDetails =
    vatTradingDetails.copy(tradingName = tradingName.getOrElse(""))
}

object TradingName extends ApiModelTransformer[TradingName] {
  val TRADING_NAME_YES = "TRADING_NAME_YES"
  val TRADING_NAME_NO = "TRADING_NAME_NO"

  implicit val format = Json.format[TradingName]

  def empty: TradingName = TradingName("", None)

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): TradingName = {

    vatScheme.tradingDetails match {
      case Some(tradingDetails) => {
        if (tradingDetails.tradingName.isEmpty) {
          TradingName(yesNo = TRADING_NAME_NO, tradingName = None)
        } else {
          TradingName(yesNo = TRADING_NAME_YES, tradingName = Some(tradingDetails.tradingName))
        }
      }

      case None => TradingName.empty
    }
  }
}
