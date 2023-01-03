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

import config.FrontendAppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class PersonalDetails(firstName: String,
                           lastName: String,
                           nino: Option[String] = None,
                           trn: Option[String] = None,
                           identifiersMatch: Boolean,
                           dateOfBirth: Option[LocalDate] = None,
                           arn: Option[String] = None,
                           score: Option[Int] = None) {
  def fullName: String = firstName + " " + lastName
}

object PersonalDetails {

  def soleTraderIdentificationReads(appConfig: FrontendAppConfig): Reads[PersonalDetails] = (
    (__ \ "fullName" \ "firstName").read[String] and
    (__ \ "fullName" \ "lastName").read[String] and
    (__ \ "nino").readNullable[String] and
    (__ \ "trn").readNullable[String] and
    (__ \ "identifiersMatch").read[Boolean] and
    (__ \ "dateOfBirth").readNullable[LocalDate] and
    Reads.pure(None) and
    (__ \ "reputation" \ appConfig.scoreKey).readNullable[Int]
  )(PersonalDetails.apply _)

  val apiReads: Reads[PersonalDetails] = (
    (__ \ "name" \ "first").read[String] orElse Reads.pure("") and
    (__ \ "name" \ "last").read[String] and
    (__ \ "nino").readNullable[String] and
    (__ \ "trn").readNullable[String] and
    (__ \ "identifiersMatch").read[Boolean] and
    (__ \ "dateOfBirth").readNullable[LocalDate] and
    (__ \ "arn").readNullable[String] and
    (__ \ "score").readNullable[Int]
  )(PersonalDetails.apply _)

  val apiWrites: Writes[PersonalDetails] = (
    (__ \ "name" \ "first").write[String] and
    (__ \ "name" \ "last").write[String] and
    (__ \ "nino").writeNullable[String] and
    (__ \ "trn").writeNullable[String] and
    (__ \ "identifiersMatch").write[Boolean] and
    (__ \ "dateOfBirth").writeNullable[LocalDate] and
    (__ \ "arn").writeNullable[String] and
    (__ \ "score").writeNullable[Int]
  )(unlift(PersonalDetails.unapply))

  implicit val apiFormat: Format[PersonalDetails] = Format(apiReads, apiWrites)

}
