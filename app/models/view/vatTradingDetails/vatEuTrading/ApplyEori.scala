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
