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

import models.api.{Address, PartyType}
import models.external.{BusinessEntity, Name}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class ApplicantDetails(personalDetails: Option[PersonalDetails] = None,
                            entity: Option[BusinessEntity] = None,
                            currentAddress: Option[Address] = None,
                            noPreviousAddress: Option[Boolean] = None,
                            previousAddress: Option[Address] = None,
                            contact: DigitalContactOptional = DigitalContactOptional(),
                            changeOfName: FormerName = FormerName(),
                            roleInTheBusiness: Option[RoleInTheBusiness] = None)

object ApplicantDetails {
  implicit val s4lKey: S4LKey[ApplicantDetails] = S4LKey("ApplicantDetails")
  implicit val apiKey: ApiKey[ApplicantDetails] = ApiKey("applicant")

  def reads(partyType: PartyType): Reads[ApplicantDetails] = (
    (__ \ "personalDetails").readNullable[PersonalDetails] and
      (__ \ "entity").readNullable[BusinessEntity](BusinessEntity.reads(partyType)) and
      (__ \ "currentAddress").readNullable[Address] and
      (__ \ "noPreviousAddress").readNullable[Boolean] and
      (__ \ "previousAddress").readNullable[Address] and
      (__ \ "contact").readWithDefault[DigitalContactOptional](DigitalContactOptional()) and
      (__ \ "changeOfName").readWithDefault[FormerName](FormerName()) and
      (__ \ "roleInTheBusiness").readNullable[RoleInTheBusiness]
    ) (ApplicantDetails.apply _)

  implicit val writes: Writes[ApplicantDetails] = (
    (__ \ "personalDetails").writeNullable[PersonalDetails] and
      (__ \ "entity").writeNullable[BusinessEntity] and
      (__ \ "currentAddress").writeNullable[Address] and
      (__ \ "noPreviousAddress").writeNullable[Boolean] and
      (__ \ "previousAddress").writeNullable[Address] and
      (__ \ "contact").write[DigitalContactOptional] and
      (__ \ "changeOfName").write[FormerName] and
      (__ \ "roleInTheBusiness").writeNullable[RoleInTheBusiness]
    ) (unlift(ApplicantDetails.unapply))

  def apiFormat(partyType: PartyType): Format[ApplicantDetails] = Format(reads(partyType), writes)

  def s4LReads(partyType: PartyType): Reads[ApplicantDetails] = {
    implicit val s4lContactReads: Reads[DigitalContactOptional] = (
      (__ \ "emailAddress" \ "email").readNullable[String] and
        (__ \ "telephoneNumber" \ "telephone").readNullable[String] and
        (__ \ "emailVerified" \ "emailVerified").readNullable[Boolean]
      ) (DigitalContactOptional.apply _)
    implicit val s4lFormerNameReads: Reads[FormerName] = (
      (__ \ "hasFormerName").readNullable[Boolean] and
        (__ \ "formerName").readNullable[Name] and
        (__ \ "formerNameDate" \ "date").readNullable[LocalDate]
      ) (FormerName.apply _)

    (
      (__ \ "personalDetails").readNullable[PersonalDetails] and
        (__ \ "entity").readNullable[BusinessEntity](BusinessEntity.reads(partyType)) and
        (__ \ "homeAddress" \ "address").readNullable[Address] and
        (__ \ "previousAddress" \ "yesNo").readNullable[Boolean] and
        (__ \ "previousAddress" \ "address").readNullable[Address] and
        (__).readWithDefault[DigitalContactOptional](DigitalContactOptional()) and
        (__).readWithDefault[FormerName](FormerName()) and
        (__ \ "roleInTheBusiness").readNullable[RoleInTheBusiness]
      ) (ApplicantDetails.apply _)
  }

}
