/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class PersonalDetails(firstName: String,
                           lastName: String,
                           nino: Option[String],
                           trn: Option[String],
                           identifiersMatch: Boolean,
                           dateOfBirth: LocalDate)

object PersonalDetails { //TODO remove all defaults here when PDV is removed
  val pdvFormat: OFormat[PersonalDetails] = (
    (__ \ "firstName").format[String] and
    (__ \ "lastName").format[String] and
    (__ \ "nino").formatNullable[String] and
    (__ \ "trn").formatNullable[String] and
    (__ \ "identifiersMatch").formatWithDefault[Boolean](true) and
    (__ \ "dateOfBirth").format[LocalDate]
    )(PersonalDetails.apply, unlift(PersonalDetails.unapply))

  val soleTraderIdentificationReads: Reads[PersonalDetails] = (
    (__ \ "fullName" \ "firstName").read[String] and
    (__ \ "fullName" \ "lastName").read[String] and
    (__ \ "nino").readNullable[String] and
    (__ \ "trn").readNullable[String] and
    (__ \ "identifiersMatch").readWithDefault[Boolean](true) and
    (__ \ "dateOfBirth").read[LocalDate]
  )(PersonalDetails.apply _)

  val apiReads: Reads[PersonalDetails] = (
    (__ \ "name" \ "first").read[String] orElse Reads.pure("") and
    (__ \ "name" \ "last").read[String] and
    (__ \ "nino").readNullable[String] and
    (__ \ "trn").readNullable[String] and
    (__ \ "identifiersMatch").readWithDefault[Boolean](true) and
    (__ \ "dateOfBirth").read[LocalDate]
  )(PersonalDetails.apply _)

  val apiWrites: Writes[PersonalDetails] = (
    (__ \ "name" \ "first").write[String] and
    (__ \ "name" \ "last").write[String] and
    (__ \ "nino").writeNullable[String] and
    (__ \ "trn").writeNullable[String] and
    (__ \ "identifiersMatch").write[Boolean] and
    (__ \ "dateOfBirth").write[LocalDate]
  )(unlift(PersonalDetails.unapply))

  implicit val apiFormat: Format[PersonalDetails] = Format(apiReads, apiWrites)

}
