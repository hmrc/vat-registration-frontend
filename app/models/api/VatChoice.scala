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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.data.format.Formats.DateTime

case class VatChoice(
                      startDate: DateTime,
                      necessity: String, // "obligatory" or "voluntary"
                      reason: Option[String]
                    )

object VatChoice {
  val r =
    (__ \ "start-date").read[String] and
      (__ \ "necessity").read[String] and
      (__ \ "reason").read[String]

  val w =
    (__ \ "start-date").write[String] and
      (__ \ "necessity").write[String] and
      (__ \ "reason").write[String]

  val apiReads: Reads[VatChoice] = r(VatChoice.apply _)
  val apiWrites: Writes[VatChoice] = w(unlift(VatChoice.unapply))

  implicit val format = Format(apiReads, apiWrites)
}
