/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officers.models.view

import java.time.LocalDate

import models.api.ScrsAddress
import models.external.Officer
import models.view.vatLodgingOfficer._
import play.api.libs.json._

case class LodgingOfficer(completionCapacity: Option[String],
                          officerSecurityQuestions: Option[OfficerSecurityQuestionsView],
                          officerHomeAddress: Option[OfficerHomeAddressView],
                          officerContactDetails: Option[OfficerContactDetailsView],
                          formerName: Option[FormerNameView],
                          formerNameDate: Option[FormerNameDateView],
                          previousAddress: Option[PreviousAddressView])

object LodgingOfficer {
  implicit val format: Format[LodgingOfficer] = Json.format[LodgingOfficer]

  def fromApi(officer: JsValue): LodgingOfficer = {
    val officerId = List((officer \ "name" \ "first").validateOpt[String].get,
      (officer \ "name" \ "last").validateOpt[String].get,
      (officer \ "name" \ "middle").validateOpt[String].get)
      .flatten
      .mkString
      .replace(" ", "")
    val officerSecurity = OfficerSecurityQuestionsView((officer \ "dob").as[LocalDate], (officer \ "nino").as[String])

    val lodgingOfficer = LodgingOfficer(
      completionCapacity = Some(officerId),
      officerSecurityQuestions = Some(officerSecurity),
      officerHomeAddress = None,
      officerContactDetails = None,
      formerName = None,
      formerNameDate = None,
      previousAddress = None
    )

    val detailsBase = (officer \ "details").validateOpt[JsObject].get
    detailsBase.fold(lodgingOfficer) { details =>
      val currentAddress: ScrsAddress = (details \ "currentAddress").as[ScrsAddress]
      val officerHomeAddress = OfficerHomeAddressView(currentAddress.id, Some(currentAddress))

      val digitalContact = OfficerContactDetailsView(
        email = Some((details \ "contact" \ "email").as[String]),
        daytimePhone = (details \ "contact" \ "tel").validateOpt[String].get,
        mobile = (details \ "contact" \ "mobile").validateOpt[String].get
      )

      lodgingOfficer.copy(
        officerHomeAddress = Some(officerHomeAddress),
        officerContactDetails = Some(digitalContact)
      )
    }
  }

  private def splitName(fullName: String): (Option[String], Option[String], Option[String]) = {
    val split = fullName.trim.split("\\s+")

    val firstName = if(fullName.trim.isEmpty) None else Some(split.head)
    val middleName = {
      val middleSplit = split
        .drop(1)
        .dropRight(1)
        .toList

      if(middleSplit.nonEmpty) Some(middleSplit.mkString(" ")) else None
    }
    val lastName = if(split.length < 2) None else Some(split.last)

    (firstName, middleName, lastName)
  }

  private def buildJsonOfficerDetails(currAddr: OfficerHomeAddressView,
                                      officerContact: OfficerContactDetailsView,
                                      formerName: FormerNameView,
                                      formerNameDate: Option[FormerNameDateView],
                                      prevAddr: PreviousAddressView): JsObject = {
    val currentAddress = Json.obj("currentAddress" ->
      Json.toJson(currAddr.address.getOrElse(throw new IllegalStateException("Missing officer current address to save into backend")))
    )

    val email = officerContact.email.fold(Json.obj())(v => Json.obj("email" -> v))
    val tel = officerContact.daytimePhone.fold(Json.obj())(v => Json.obj("tel" -> v))
    val mobile = officerContact.mobile.fold(Json.obj())(v => Json.obj("mobile" -> v))
    val contact = Json.obj("contact" -> email.++(tel).++(mobile))

    val previousAddress = if (!prevAddr.yesNo) Json.obj() else {
      Json.obj("previousAddress" ->
        Json.toJson(prevAddr.address.getOrElse(throw new IllegalStateException("Missing officer previous address to save into backend"))).as[JsObject]
      )
    }
    val changeOfName = if (!formerName.yesNo) Json.obj() else {
      val (first, middle, last) = splitName(formerName.formerName.getOrElse(
        throw new IllegalStateException("Missing officer former name to save into backend")
      ))
      val jsonFirstName = first.fold(Json.obj())(fn => Json.obj("first" -> fn))
      val jsonMiddleName = middle.fold(Json.obj())(mn => Json.obj("middle" -> mn))
      val jsonLastName = last.fold(Json.obj())(sn => Json.obj("last" -> sn))
      val jsonFormerName = Json.obj("name" -> jsonFirstName.++(jsonMiddleName).++(jsonLastName))

      val changeDate = formerNameDate.getOrElse(throw new IllegalStateException("Missing officer former name change date to save into backend"))
      val jsonChangeDate = Json.obj("change" -> changeDate.date)

      Json.obj("changeOfName" -> jsonFormerName.++(jsonChangeDate))
    }

    currentAddress ++ contact ++ changeOfName ++ previousAddress
  }

  def apiWrites(officer: Officer): Writes[LodgingOfficer] = new Writes[LodgingOfficer] {
    override def writes(o: LodgingOfficer) = {
      val lastName = Json.obj("last" -> officer.name.surname)
      val firstName = officer.name.forename.fold(Json.obj())(v => Json.obj("first" -> v))
      val middleName = officer.name.otherForenames.fold(Json.obj())(v => Json.obj("middle" -> v))
      val name = Json.obj("name" -> lastName.++(firstName).++(middleName))

      val officerSecurityQuestions = o.officerSecurityQuestions
        .getOrElse(throw new IllegalStateException("Missing officer security data to save into backend"))

      val otherData = Json.parse(
        s"""
           |{
           |  "role": "${officer.role}",
           |  "dob": "${officerSecurityQuestions.dob}",
           |  "nino": "${officerSecurityQuestions.nino}"
           |}
        """.stripMargin
      ).as[JsObject]

      val officerDetails = o match {
        case LodgingOfficer(_, _, None, None, None, None, None) =>
          Json.obj()
        case LodgingOfficer(_, _, Some(currAddr), Some(contact), Some(formerName), _@nameChangeDate, Some(prevAddr)) =>
          Json.obj("details" -> buildJsonOfficerDetails(currAddr, contact, formerName, nameChangeDate, prevAddr))
        case _ =>
          throw new IllegalStateException("Missing officer details data to save into backend")
      }

      name ++ otherData ++ officerDetails
    }
  }
}
