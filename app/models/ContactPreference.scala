/*
 * Copyright 2021 HM Revenue & Customs
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

sealed trait ContactPreference

case object Email extends ContactPreference

case object Letter extends ContactPreference

object ContactPreference {
  val email = "Email"
  val letter = "Letter"

  implicit val reads: Reads[ContactPreference] = Reads[ContactPreference] {
    case JsString(`email`) => JsSuccess(Email)
    case JsString(`letter`) => JsSuccess(Letter)
    case _ => JsError("Could not parse contact preference")
  }

  implicit val writes: Writes[ContactPreference] = Writes[ContactPreference] {
    case Email => JsString(email)
    case Letter => JsString(letter)
  }

  implicit val format: Format[ContactPreference] = Format(reads, writes)
}