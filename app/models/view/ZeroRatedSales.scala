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

import models.ApiModelTransformer
import models.api.VatScheme
import play.api.libs.json.Json

case class ZeroRatedSales(yesNo: String) {
}

object ZeroRatedSales extends ApiModelTransformer[ZeroRatedSales] {
  val ZERO_RATED_SALES_YES = "ZERO_RATED_SALES_YES"
  val ZERO_RATED_SALES_NO = "ZERO_RATED_SALES_NO"

  implicit val format = Json.format[ZeroRatedSales]

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): ZeroRatedSales =

    vatScheme.financials match {

      case Some(financials) => financials.zeroRatedSalesEstimate match {
        case Some(_) => ZeroRatedSales(ZERO_RATED_SALES_YES)
        case None => ZeroRatedSales(ZERO_RATED_SALES_NO)
      }

      case None => ZeroRatedSales.empty
    }

  def empty: ZeroRatedSales = ZeroRatedSales("")
}
