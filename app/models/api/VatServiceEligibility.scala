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

import models._
import models.api.EligibilityQuestion._
import play.api.libs.json.{Json, OFormat}

final case class VatServiceEligibility(
                                        haveNino: Option[Boolean] = None,
                                        doingBusinessAbroad: Option[Boolean] = None,
                                        doAnyApplyToYou: Option[Boolean] = None,
                                        applyingForAnyOf: Option[Boolean] = None,
                                        companyWillDoAnyOf: Option[Boolean] = None
                                      ) {

  def getAnswer(question: EligibilityQuestion): Option[Boolean] = question match {
    case HaveNinoQuestion => haveNino
    case DoingBusinessAbroadQuestion => doingBusinessAbroad
    case DoAnyApplyToYouQuestion => doAnyApplyToYou
    case ApplyingForAnyOfQuestion => applyingForAnyOf
    case CompanyWillDoAnyOfQuestion => companyWillDoAnyOf
  }

  def setAnswer(question: EligibilityQuestion, answer: Boolean): VatServiceEligibility = question match {
    case HaveNinoQuestion => this.copy(haveNino = Some(answer))
    case DoingBusinessAbroadQuestion => this.copy(doingBusinessAbroad = Some(answer))
    case DoAnyApplyToYouQuestion => this.copy(doAnyApplyToYou = Some(answer))
    case ApplyingForAnyOfQuestion => this.copy(applyingForAnyOf = Some(answer))
    case CompanyWillDoAnyOfQuestion => this.copy(companyWillDoAnyOf = Some(answer))
  }

}


object VatServiceEligibility {


  implicit val format: OFormat[VatServiceEligibility] = Json.format

  implicit val vmReads = ViewModelFormat(
    readF = (group: S4LVatEligibility) => group.vatEligibility,
    updateF = (c: VatServiceEligibility, g: Option[S4LVatEligibility]) =>
      g.getOrElse(S4LVatEligibility()).copy(vatEligibility = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[VatServiceEligibility] { vs: VatScheme =>
    vs.vatServiceEligibility
  }

}

