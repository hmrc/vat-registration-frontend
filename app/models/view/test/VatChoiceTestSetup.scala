/*
 * Copyright 2018 HM Revenue & Customs
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

package models.view.test

import play.api.libs.json.Json

case class VatChoiceTestSetup(taxableTurnoverChoice: Option[String],
                              voluntaryChoice: Option[String],
                              voluntaryRegistrationReason: Option[String],
                              startDateChoice: Option[String],
                              startDateDay: Option[String],
                              startDateMonth: Option[String],
                              startDateYear: Option[String],
                              overThresholdSelection: Option[String],
                              overThresholdMonth: Option[String],
                              overThresholdYear: Option[String])

object VatChoiceTestSetup {
  implicit val format = Json.format[VatChoiceTestSetup]
}
