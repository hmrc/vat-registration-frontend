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

import models.{ApiModelTransformer, ViewModelTransformer}
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

  def getValue(field: String, eligibility: VatServiceEligibility): Option[Boolean] = {
    field match {
      case "haveNino" => eligibility.haveNino
      case "doingBusinessAbroad" => eligibility.doingBusinessAbroad
      case "doAnyApplyToYou" => eligibility.doAnyApplyToYou
      case "applyingForAnyOf" => eligibility.applyingForAnyOf
      case "companyWillDoAnyOf" => eligibility.companyWillDoAnyOf
    }
  }

  def setValue(field: String, value: Option[Boolean], eligibility: VatServiceEligibility): VatServiceEligibility = {
    field match {
      case "haveNino" => eligibility.copy(haveNino = value)
      case "doingBusinessAbroad" => eligibility.copy(doingBusinessAbroad = value)
      case "doAnyApplyToYou" => eligibility.copy(doAnyApplyToYou = value)
      case "applyingForAnyOf" => eligibility.copy(applyingForAnyOf = value)
      case "companyWillDoAnyOf" => eligibility.copy(companyWillDoAnyOf = value)
    }
  }

  implicit val modelTransformer = ApiModelTransformer[VatServiceEligibility] { vs: VatScheme =>
    vs.vatServiceEligibility
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VatServiceEligibility, g: VatServiceEligibility) =>
    g.copy(haveNino = c.haveNino,
      doingBusinessAbroad = c.doingBusinessAbroad,
      doAnyApplyToYou = c.doAnyApplyToYou,
      applyingForAnyOf = c.applyingForAnyOf,
      companyWillDoAnyOf = c.companyWillDoAnyOf)
  }

  // TODO remove once no longer required
  val empty = VatServiceEligibility(None, None, None, None, None)

}

