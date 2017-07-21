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

package models.view.vatFinancials

import models.api.VatScheme
import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
import play.api.libs.json.{Json, OFormat}

case class EstimateZeroRatedSales(zeroRatedTurnoverEstimate: Long)

object EstimateZeroRatedSales {

  implicit val format: OFormat[EstimateZeroRatedSales] = Json.format[EstimateZeroRatedSales]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatFinancials) => group.zeroRatedTurnoverEstimate,
    updateF = (c: EstimateZeroRatedSales, g: Option[S4LVatFinancials]) =>
      g.getOrElse(S4LVatFinancials()).copy(zeroRatedTurnoverEstimate = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[EstimateZeroRatedSales] { (vs: VatScheme) =>
    vs.financials.map(_.zeroRatedTurnoverEstimate).collect {
      case Some(sales) => EstimateZeroRatedSales(sales)
    }
  }

}
