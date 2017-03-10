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

import enums.CacheKeys
import models.api.{VatScheme, VatTradingDetails}
import models.{ApiModelTransformer, CacheKey, ViewModelTransformer}
import play.api.libs.json.Json

case class TradingName(yesNo: String,
                       tradingName: Option[String]) {
  override def toString: String = tradingName.getOrElse("")
}

object TradingName {

  val TRADING_NAME_YES = "TRADING_NAME_YES"
  val TRADING_NAME_NO = "TRADING_NAME_NO"

  val valid = (item: String) => List(TRADING_NAME_YES, TRADING_NAME_NO).contains(item.toUpperCase)

  implicit val format = Json.format[TradingName]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.tradingDetails.map {
      _.tradingName match {
        case tn if !tn.isEmpty => TradingName(TRADING_NAME_YES, tradingName = Some(tn))
        case _ => TradingName(TRADING_NAME_NO, tradingName = None)
      }
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: TradingName, g: VatTradingDetails) =>
    g.copy(tradingName = c.tradingName.getOrElse(""))
  }

  implicit val cacheKey = CacheKey[TradingName](CacheKeys.TradingName)

}
