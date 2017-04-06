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

package models.view.vatTradingDetails.vatEuTrading

import models.api._
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class EuGoods(yesNo: String)

object EuGoods {

  val EU_GOODS_YES = "EU_GOODS_YES"
  val EU_GOODS_NO = "EU_GOODS_NO"

  val valid = (item: String) => List(EU_GOODS_YES, EU_GOODS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[EuGoods]

  implicit val modelTransformer = ApiModelTransformer[EuGoods] { (vs: VatScheme) =>
    vs.tradingDetails.map(td => EuGoods(if (td.euTrading.selection) EU_GOODS_YES else EU_GOODS_NO))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: EuGoods, g: VatTradingDetails) =>
    g.copy(euTrading = VatEuTrading(c.yesNo == EU_GOODS_YES))
  }

}



