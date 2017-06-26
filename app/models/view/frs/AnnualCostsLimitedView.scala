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

import models._
import models.api.{VatFlatRateSchemeAnswers, VatScheme}
import play.api.libs.json.Json

case class AnnualCostsLimitedView(selection: String)

object AnnualCostsLimitedView {

  val YES = "yes"
  val YES_WITHIN_12_MONTHS = "yesWithin12months"
  val NO = "no"

  val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

  implicit val format = Json.format[AnnualCostsLimitedView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateScheme) => group.annualCostsInclusive,
    updateF = (c: AnnualCostsLimitedView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(annualCostsLimited = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[AnnualCostsLimitedView] { vs: VatScheme =>
    vs.vatFlatRateSchemeAnswers.flatMap(_.annualCostsInclusive).collect {
      case YES => AnnualCostsLimitedView(YES)
      case YES_WITHIN_12_MONTHS => AnnualCostsLimitedView(YES_WITHIN_12_MONTHS)
      case NO => AnnualCostsLimitedView(NO)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AnnualCostsLimitedView, g: VatFlatRateSchemeAnswers) =>
    g.copy(annualCostsLimited = Some(c.selection))
  }

}