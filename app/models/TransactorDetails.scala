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

import java.time.LocalDate

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TransactorDetails(firstName: String,
                             lastName: String,
                             nino: String,
                             dateOfBirth: LocalDate,
                             role: Option[String] = None)

object TransactorDetails {
  implicit val format: OFormat[TransactorDetails] = Json.format[TransactorDetails]

  val apiReads: Reads[TransactorDetails] = (
    (__ \ "name" \ "first").read[String] orElse Reads.pure("") and
    (__ \ "name" \ "last").read[String] and
    (__ \ "nino").read[String] and
    (__ \ "dateOfBirth").read[LocalDate] and
    (__ \ "role").readNullable[String]
  )(TransactorDetails.apply(_, _, _, _, _))

  val apiWrites: Writes[TransactorDetails] = (
    (__ \ "name" \ "first").write[String] and
    (__ \ "name"\ "last").write[String] and
    (__ \ "nino").write[String] and
    (__ \ "dateOfBirth").write[LocalDate] and
    (__ \ "role").writeNullable[String]
  )(unlift(TransactorDetails.unapply))

  val apiFormat: Format[TransactorDetails] = Format(apiReads, apiWrites)

}
