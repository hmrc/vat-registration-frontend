/*
 * Copyright 2023 HM Revenue & Customs
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

package bars.model.response

import play.api.libs.json.{Json, OFormat}

final case class BarsValidateResponse(
  accountNumberIsWellFormatted:             BarsAssessmentType,
  nonStandardAccountDetailsRequiredForBacs: BarsAssessmentType,
  sortCodeIsPresentOnEISCD:                 BarsAssessmentType,
  sortCodeSupportsDirectDebit:              Option[BarsAssessmentType],
  sortCodeSupportsDirectCredit:             Option[BarsAssessmentType] = None,
  sortCodeBankName:                         Option[String] = None,
  iban:                                     Option[String] = None
)

object BarsValidateResponse {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[BarsValidateResponse] = Json.format

}
