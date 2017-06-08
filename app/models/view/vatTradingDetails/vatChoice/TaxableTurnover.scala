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

package models.view.vatTradingDetails.vatChoice

import models.{ApiModelTransformer, S4LTradingDetails, ViewModelFormat}
import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api.VatScheme
import play.api.libs.json.Json

case class TaxableTurnover(yesNo: String)

object TaxableTurnover {

  val TAXABLE_YES = "TAXABLE_YES"
  val TAXABLE_NO = "TAXABLE_NO"

  val valid = (item: String) => List(TAXABLE_YES, TAXABLE_NO).contains(item.toUpperCase)

  implicit val format = Json.format[TaxableTurnover]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LTradingDetails) => group.taxableTurnover,
    updateF = (c: TaxableTurnover, g: Option[S4LTradingDetails]) =>
      g.getOrElse(S4LTradingDetails()).copy(taxableTurnover = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[TaxableTurnover] { (vs: VatScheme) =>
    vs.tradingDetails.map(_.vatChoice.necessity).collect {
      case NECESSITY_VOLUNTARY => TaxableTurnover(TAXABLE_NO)
      case NECESSITY_OBLIGATORY => TaxableTurnover(TAXABLE_YES)
    }
  }

}
