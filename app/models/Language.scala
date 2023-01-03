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

package models

import play.api.libs.json._

sealed trait Language

case object English extends Language

case object Welsh extends Language

object Language {
  val english = "English"
  val welsh = "Welsh"

  implicit val reads: Reads[Language] = Reads[Language] {
    case JsString(`english`) => JsSuccess(English)
    case JsString(`welsh`) => JsSuccess(Welsh)
    case _ => JsError("Could not parse contact preference")
  }

  implicit val writes: Writes[Language] = Writes[Language] {
    case English => JsString(english)
    case Welsh => JsString(welsh)
  }

  implicit val format: Format[Language] = Format(reads, writes)
}