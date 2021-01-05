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

package models.view

import java.time.LocalDate

import models.{S4LKey, TelephoneNumber, TransactorDetails}
import models.api.Address
import models.external.incorporatedentityid.IncorporationDetails
import models.external.{EmailAddress, EmailVerified, Name}
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ApplicantDetails(incorporationDetails: Option[IncorporationDetails] = None,
                            transactorDetails: Option[TransactorDetails] = None,
                            homeAddress: Option[HomeAddressView] = None,
                            emailAddress: Option[EmailAddress] = None,
                            emailVerified: Option[EmailVerified] = None,
                            telephoneNumber: Option[TelephoneNumber] = None,
                            formerName: Option[FormerNameView] = None,
                            formerNameDate: Option[FormerNameDateView] = None,
                            previousAddress: Option[PreviousAddressView] = None)

object ApplicantDetails {
  implicit val format: Format[ApplicantDetails] = Json.format[ApplicantDetails]
  implicit val s4lKey: S4LKey[ApplicantDetails] = S4LKey("ApplicantDetails")

  val apiReads: Reads[ApplicantDetails] = (
    JsPath.readNullable[IncorporationDetails].orElse(Reads.pure(None)) and
    JsPath.readNullable[TransactorDetails](TransactorDetails.apiFormat).orElse(Reads.pure(None)) and
    (__ \ "currentAddress").readNullable[Address].fmap(_.map(addr => HomeAddressView(addr.id, Some(addr)))) and
    (__ \ "contact" \ "email").readNullable[String].fmap(_.map(EmailAddress(_))) and
    (__ \ "contact" \ "emailVerified").readNullable[Boolean].fmap(_.map(EmailVerified(_))) and
    (__ \ "contact" \ "tel").readNullable[String].fmap(_.map(TelephoneNumber(_))) and
    (__ \ "changeOfName" \ "name").readNullable[Name]
      .fmap(con => Some(FormerNameView(con.isDefined, con.map(name => name.asLabel)))) and
    (__ \ "changeOfName" \ "change").readNullable[LocalDate]
      .fmap(cond => cond.map(FormerNameDateView(_))) and
    (__ \ "previousAddress").readNullable[Address].fmap(address => Some(PreviousAddressView(address.isEmpty, address)))
  )(ApplicantDetails.apply _)

  val apiWrites: Writes[ApplicantDetails] = (
    JsPath.writeNullable[IncorporationDetails] and
    JsPath.writeNullable[TransactorDetails](TransactorDetails.apiFormat) and
    (__ \ "currentAddress").writeNullable[Address].contramap[Option[HomeAddressView]](_.flatMap(_.address)) and
    (__ \ "contact" \ "email").writeNullable[String].contramap[Option[EmailAddress]](_.map(_.email)) and
    (__ \ "contact" \ "emailVerified").writeNullable[Boolean].contramap[Option[EmailVerified]](_.map(_.emailVerified)) and
    (__ \ "contact" \ "tel").writeNullable[String].contramap[Option[TelephoneNumber]](_.map(_.telephone)) and
    (__ \ "changeOfName" \ "name").writeNullable[Name].contramap[Option[FormerNameView]](_.flatMap(_.formerName.map(splitName))) and
    (__ \ "changeOfName" \ "change").writeNullable[LocalDate].contramap[Option[FormerNameDateView]](_.map(_.date)) and
    (__ \ "previousAddress").writeNullable[Address].contramap[Option[PreviousAddressView]](_.flatMap(_.address))
  )(unlift(ApplicantDetails.unapply))

  val apiFormat: Format[ApplicantDetails] = Format(
    apiReads,
    apiWrites
  )

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
