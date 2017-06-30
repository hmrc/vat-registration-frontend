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

case class AnnualCostsInclusiveView(selection: String)

object AnnualCostsInclusiveView {

  val YES = "yes"
  val YES_WITHIN_12_MONTHS = "yesWithin12months"
  val NO = "no"

  val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

  implicit val format = Json.format[AnnualCostsInclusiveView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateSchemeAnswers) => group.annualCostsInclusive,
    updateF = (c: AnnualCostsInclusiveView, g: Option[S4LFlatRateSchemeAnswers]) =>
      g.getOrElse(S4LFlatRateSchemeAnswers()).copy(annualCostsInclusive = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[AnnualCostsInclusiveView] { vs: VatScheme =>
    vs.vatFlatRateSchemeAnswers.flatMap(_.annualCostsInclusive).collect {
      case YES => AnnualCostsInclusiveView(YES)
      case YES_WITHIN_12_MONTHS => AnnualCostsInclusiveView(YES_WITHIN_12_MONTHS)
      case NO => AnnualCostsInclusiveView(NO)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AnnualCostsInclusiveView, g: VatFlatRateSchemeAnswers) =>
    g.copy(annualCostsInclusive = Some(c.selection))
  }

}