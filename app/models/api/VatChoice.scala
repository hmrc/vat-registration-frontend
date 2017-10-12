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

import play.api.libs.json._

case class VatChoice(vatStartDate: VatStartDate)

import models.api.VatEligibilityChoice.NECESSITY_VOLUNTARY

case class VatEligibilityChoice(necessity: String, // "obligatory" or "voluntary"
                                reason: Option[String] = None,
                                vatThresholdPostIncorp: Option[VatThresholdPostIncorp] = None,
                                vatExpectedThresholdPostIncorp: Option[VatExpectedThresholdPostIncorp] = None) {
  def registeringVoluntarily: Boolean = necessity == NECESSITY_VOLUNTARY
}

object VatEligibilityChoice {

  val NECESSITY_OBLIGATORY = "obligatory"
  val NECESSITY_VOLUNTARY = "voluntary"

  implicit val format: OFormat[VatEligibilityChoice] = Json.format[VatEligibilityChoice]



}
object VatChoice {
  implicit val format: OFormat[VatChoice] = Json.format[VatChoice]
}

