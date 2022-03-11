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
import models.external.{BusinessEntity, EmailAddress, EmailVerified, Name}
import models.view.{FormerNameDateView, HomeAddressView, PreviousAddressView}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class ApplicantDetails(entity: Option[BusinessEntity] = None,
                            personalDetails: Option[PersonalDetails] = None,
                            homeAddress: Option[HomeAddressView] = None,
                            emailAddress: Option[EmailAddress] = None,
                            emailVerified: Option[EmailVerified] = None,
                            telephoneNumber: Option[TelephoneNumber] = None,
                            hasFormerName: Option[Boolean] = None,
                            formerName: Option[Name] = None,
                            formerNameDate: Option[FormerNameDateView] = None,
                            previousAddress: Option[PreviousAddressView] = None,
                            roleInTheBusiness: Option[RoleInTheBusiness] = None)

object ApplicantDetails {
  implicit val s4lKey: S4LKey[ApplicantDetails] = S4LKey("ApplicantDetails")

  def reads(partyType: PartyType): Reads[ApplicantDetails] = (
    (__ \ "entity").readNullable[BusinessEntity](BusinessEntity.reads(partyType)) and
      (__ \ "personalDetails").readNullable[PersonalDetails](PersonalDetails.apiFormat).orElse(Reads.pure(None)) and
      (__ \ "currentAddress").readNullable[Address].fmap(_.map(addr => HomeAddressView(addr.id, Some(addr)))) and
      (__ \ "contact" \ "email").readNullable[String].fmap(_.map(EmailAddress(_))) and
      (__ \ "contact" \ "emailVerified").readNullable[Boolean].fmap(_.map(EmailVerified(_))) and
      (__ \ "contact" \ "tel").readNullable[String].fmap(_.map(TelephoneNumber(_))) and
      (__ \ "changeOfName" \ "hasFormerName").readNullable[Boolean] and
      (__ \ "changeOfName" \ "name").readNullable[Name] and
      (__ \ "changeOfName" \ "change").readNullable[LocalDate]
        .fmap(cond => cond.map(FormerNameDateView(_))) and
      (__ \ "previousAddress").readNullable[Address].fmap(address => Some(PreviousAddressView(address.isEmpty, address))) and
      (__ \ "roleInTheBusiness").readNullable[RoleInTheBusiness]
    ) (ApplicantDetails.apply _)

  val writes: Writes[ApplicantDetails] = (
    (__ \ "entity").writeNullable[BusinessEntity](BusinessEntity.writes) and
      (__ \ "personalDetails").writeNullable[PersonalDetails](PersonalDetails.apiFormat) and
      (__ \ "currentAddress").writeNullable[Address].contramap[Option[HomeAddressView]](_.flatMap(_.address)) and
      (__ \ "contact" \ "email").writeNullable[String].contramap[Option[EmailAddress]](_.map(_.email)) and
      (__ \ "contact" \ "emailVerified").writeNullable[Boolean].contramap[Option[EmailVerified]](_.map(_.emailVerified)) and
      (__ \ "contact" \ "tel").writeNullable[String].contramap[Option[TelephoneNumber]](_.map(_.telephone)) and
      (__ \ "changeOfName" \ "hasFormerName").writeNullable[Boolean] and
      (__ \ "changeOfName" \ "name").writeNullable[Name] and
      (__ \ "changeOfName" \ "change").writeNullable[LocalDate].contramap[Option[FormerNameDateView]](_.map(_.date)) and
      (__ \ "previousAddress").writeNullable[Address].contramap[Option[PreviousAddressView]](_.flatMap(_.address)) and
      (__ \ "roleInTheBusiness").writeNullable[RoleInTheBusiness]
    ) (unlift(ApplicantDetails.unapply))

  def s4LReads(partyType: PartyType): Reads[ApplicantDetails] = {
    implicit val businessEntityReads: Reads[BusinessEntity] = BusinessEntity.reads(partyType)
    Json.reads[ApplicantDetails]
  }
  implicit val s4LWrites: Writes[ApplicantDetails] = Json.writes[ApplicantDetails]

  private def splitName(fullName: String): Name = {
    val split = fullName.trim.split("\\s+")

    val middleName = {
      val middleSplit = split
        .drop(1)
        .dropRight(1)
        .toList

      if (middleSplit.nonEmpty) Some(middleSplit.mkString(" ")) else None
    }
    val firstName = if (split.length < 2) None else Some(split.head)

    Name(firstName, middleName, split.last)
  }

}
