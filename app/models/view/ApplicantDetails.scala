/*
 * Copyright 2020 HM Revenue & Customs
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

import models.S4LKey
import models.api.ScrsAddress
import models.external.Name
import play.api.libs.json.{Json, _}
import play.api.libs.functional.syntax._

case class ApplicantDetails(homeAddress: Option[HomeAddressView],
                          contactDetails: Option[ContactDetailsView],
                          formerName: Option[FormerNameView],
                          formerNameDate: Option[FormerNameDateView],
                          previousAddress: Option[PreviousAddressView])

object ApplicantDetails {
  implicit val format: Format[ApplicantDetails] = Json.format[ApplicantDetails]
  implicit val s4lKey: S4LKey[ApplicantDetails] = S4LKey("ApplicantDetails")

  val apiReads: Reads[ApplicantDetails] = (
    (__ \ "currentAddress").readNullable[ScrsAddress]
      .fmap(_.map(addr => HomeAddressView(addr.id, Some(addr)))) and
    (__ \ "contact").readNullable[ContactDetailsView] and
    (__ \ "changeOfName" \ "name").readNullable[Name]
      .fmap(con => con.map(name => FormerNameView(con.isDefined, Some(name.asLabel))))
      .orElse(Reads.pure(Some(FormerNameView(false, None)))) and
    (__ \ "changeOfName" \ "change").readNullable[LocalDate]
      .fmap(cond => cond.map(FormerNameDateView(_)))
      .orElse(Reads.pure(None)) and
    (__ \ "previousAddress").readNullable[ScrsAddress]
      .fmap(address => Some(PreviousAddressView(address.isEmpty, address)))
  )(ApplicantDetails.apply(_, _, _, _, _))
    .map(details => if (details.formerName.isEmpty) details.copy(formerNameDate = None) else details)

  val apiWrites: Writes[ApplicantDetails] = (
    (__ \ "currentAddress").writeNullable[ScrsAddress]
      .contramap[Option[HomeAddressView]](_.flatMap(_.address)) and
    (__ \ "contact").writeNullable[ContactDetailsView] and
    (__ \ "changeOfName" \ "name").writeNullable[Name]
      .contramap[Option[FormerNameView]](_.flatMap(_.formerName.map(splitName))) and
    (__ \ "changeOfName" \ "change").writeNullable[LocalDate]
      .contramap[Option[FormerNameDateView]](_.map(_.date)) and
    (__ \ "previousAddress").writeNullable[ScrsAddress]
      .contramap[Option[PreviousAddressView]](_.flatMap(_.address))
  )(unlift(ApplicantDetails.unapply))
    .contramap(details => if (details.formerName.isEmpty) details.copy(formerNameDate = None) else details)

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
