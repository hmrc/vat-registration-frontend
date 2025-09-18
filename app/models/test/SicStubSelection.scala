/*
 * Copyright 2024 HM Revenue & Customs
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

package models.test

import play.api.libs.json._

sealed trait SicStubSelection

case object SingleSicCode extends SicStubSelection

case object SingleSicCodeCompliance extends SicStubSelection

case object MultipleSicCodeNoCompliance extends SicStubSelection

case object MultipleSicCodeCompliance extends SicStubSelection

case object CustomSicCodes extends SicStubSelection

object SicStubSelection {
  val single = "SingleSicCode"
  val singleCompliance = "SingleSicCodeCompliance"
  val multipleNoCompliance = "MultipleSicCodeNoCompliance"
  val multipleCompliance = "MultipleSicCodeCompliance"
  val custom = "CustomSicCodes"

  implicit val format: Format[SicStubSelection] = Format[SicStubSelection](
    Reads[SicStubSelection] {
      case JsString(`single`) => JsSuccess(SingleSicCode)
      case JsString(`singleCompliance`) => JsSuccess(SingleSicCodeCompliance)
      case JsString(`multipleNoCompliance`) => JsSuccess(MultipleSicCodeNoCompliance)
      case JsString(`multipleCompliance`) => JsSuccess(MultipleSicCodeCompliance)
      case JsString(`custom`) => JsSuccess(CustomSicCodes)
      case _ => JsError(error = "Invalid SicStubSelection value")
    },
    Writes[SicStubSelection] {
      case SingleSicCode => JsString(single)
      case SingleSicCodeCompliance => JsString(singleCompliance)
      case MultipleSicCodeNoCompliance => JsString(multipleNoCompliance)
      case MultipleSicCodeCompliance => JsString(multipleCompliance)
      case CustomSicCodes => JsString(custom)
    }
  )
}
