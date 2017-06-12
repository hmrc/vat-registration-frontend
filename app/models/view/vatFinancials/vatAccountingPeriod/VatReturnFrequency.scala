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

package models.view.vatFinancials.vatAccountingPeriod

import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class VatReturnFrequency(frequencyType: String)

object VatReturnFrequency {

  val MONTHLY = "monthly"
  val QUARTERLY = "quarterly"

  val monthly = VatReturnFrequency(MONTHLY)
  val quarterly = VatReturnFrequency(QUARTERLY)

  val valid = (item: String) => List(MONTHLY, QUARTERLY).contains(item.toLowerCase)

  implicit val format = Json.format[VatReturnFrequency]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatFinancials) => group.vatReturnFrequency,
    updateF = (c: VatReturnFrequency, g: Option[S4LVatFinancials]) =>
      g.getOrElse(S4LVatFinancials()).copy(vatReturnFrequency = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    vs.financials map (vf => VatReturnFrequency(vf.accountingPeriods.frequency))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VatReturnFrequency, g: VatFinancials) =>
    g.copy(accountingPeriods = g.accountingPeriods.copy(frequency = c.frequencyType))
  }

}