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

package models.api

import models.ViewModelTransformer
import play.api.libs.json.{Json, OFormat}

final case class VatServiceEligibility(
                                        haveNino: Option[Boolean] = None,
                                        doingBusinessAbroad: Option[Boolean] = None,
                                        doAnyApplyToYou: Option[Boolean] = None,
                                        applyingForAnyOf: Option[Boolean] = None,
                                        companyWillDoAnyOf: Option[Boolean] = None
                                      )

object VatServiceEligibility {

  implicit val format: OFormat[VatServiceEligibility] = Json.format

  def apply(field: String, value: Boolean): VatServiceEligibility = {
    val vatServiceEligibility = VatServiceEligibility()

    field match {
      case "haveNino" => vatServiceEligibility.copy(haveNino = Some(value))
      case "doingBusinessAbroad" => vatServiceEligibility.copy(doingBusinessAbroad = Some(value))
      case "doAnyApplyToYou" => vatServiceEligibility.copy(doAnyApplyToYou = Some(value))
      case "applyingForAnyOf" => vatServiceEligibility.copy(applyingForAnyOf = Some(value))
      case "companyWillDoAnyOf" => vatServiceEligibility.copy(companyWillDoAnyOf = Some(value))
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VatServiceEligibility, g: VatServiceEligibility) =>
    g.copy(haveNino = c.haveNino,
      doingBusinessAbroad = c.doingBusinessAbroad,
      doAnyApplyToYou = c.doAnyApplyToYou,
      applyingForAnyOf = c.applyingForAnyOf,
      companyWillDoAnyOf = c.companyWillDoAnyOf)
  }

}

