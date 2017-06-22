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

package models.view.frs

import models.api.{FlatRateScheme, VatScheme, VatTradingDetails}
import models._
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason.{INTENDS_TO_SELL, SELLS, intendsToSell, sells}
import play.api.libs.json.Json

case class AnnualCostsInclusiveView(selection: String)

object AnnualCostsInclusiveView {

  val YES = "YES"
  val NO_NEXT_TWELVE_MONTHS = "NO_NEXT_TWELVE_MONTHS"
  val NO = "NO"

  val valid: (String) => Boolean = List(YES, NO_NEXT_TWELVE_MONTHS, NO).contains

  implicit val format = Json.format[AnnualCostsInclusiveView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateScheme) => group.annualCostsInclusive,
    updateF = (c: AnnualCostsInclusiveView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(annualCostsInclusive = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[AnnualCostsInclusiveView] { vs: VatScheme =>
    None
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AnnualCostsInclusiveView, g: FlatRateScheme) =>
    g
  }

}