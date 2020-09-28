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

import deprecated.DeprecatedConstants
import models.S4LKey
import models.api.ScrsAddress
import models.external.Name
import play.api.libs.json._

case class ApplicantDetails(homeAddress: Option[HomeAddressView],
                          contactDetails: Option[ContactDetailsView],
                          formerName: Option[FormerNameView],
                          formerNameDate: Option[FormerNameDateView],
                          previousAddress: Option[PreviousAddressView])

object ApplicantDetails {
  implicit val format: Format[ApplicantDetails] = Json.format[ApplicantDetails]
  implicit val s4lKey: S4LKey[ApplicantDetails] = S4LKey("ApplicantDetails")

  def fromJsonToName(json: JsValue): Name = Name(
    first = (json \ "name" \ "first").validateOpt[String].get,
    middle = (json \ "name" \ "middle").validateOpt[String].get,
    last = (json \ "name" \ "last").validate[String].get,
    title = None
  )

  def fromApi(applicantDetails: JsValue): ApplicantDetails = {

    val applicantDetailsView = ApplicantDetails(
      homeAddress = None,
      contactDetails = None,
      formerName = None,
      formerNameDate = None,
      previousAddress = None
    )

    val email = (applicantDetails \ "contact" \ "email").validateOpt[String].get
    val daytimePhone = (applicantDetails \ "contact" \ "tel").validateOpt[String].get
    val mobile = (applicantDetails \ "contact" \ "mobile").validateOpt[String].get

    val currentAddress = (applicantDetails \ "currentAddress").validateOpt[ScrsAddress].get
    val homeAddress = currentAddress match {
      case None =>
        None
      case Some(address) =>
        Some(HomeAddressView(address.id, currentAddress))
    }

    val digitalContact = ContactDetailsView(daytimePhone, email, mobile)
    val contactDetailsView = if (mobile.isEmpty && daytimePhone.isEmpty && email.isEmpty) None else Some(
      ContactDetailsView(daytimePhone, email, mobile)
    )

    val changeOfName: Option[JsObject] = (applicantDetails \ "changeOfName").validateOpt[JsObject].get

    val formerNameView = FormerNameView(changeOfName.isDefined, changeOfName.map(fromJsonToName(_).asLabel))

    val formerNameDateView: Option[FormerNameDateView] = changeOfName.map(json => FormerNameDateView((json \ "change").as[LocalDate]))

    val previousAddress = (applicantDetails \ "previousAddress").validateOpt[ScrsAddress].get

    applicantDetailsView.copy(
      homeAddress = homeAddress,
      contactDetails = contactDetailsView,
      formerName = Some(formerNameView),
      formerNameDate = formerNameDateView,
      previousAddress = Some(PreviousAddressView(previousAddress.isEmpty, previousAddress))
    )
  }

  private def splitName(fullName: String): (Option[String], Option[String], Option[String]) = {
    val split = fullName.trim.split("\\s+")

    val lastName = if (fullName.trim.isEmpty) None else Some(split.last)
    val middleName = {
      val middleSplit = split
        .drop(1)
        .dropRight(1)
        .toList

      if (middleSplit.nonEmpty) Some(middleSplit.mkString(" ")) else None
    }
    val firstName = if (split.length < 2) None else Some(split.head)

    (firstName, middleName, lastName)
  }

  private def buildJsonApplicantDetails(currAddr: HomeAddressView,
                                      applicantContact: ContactDetailsView,
                                      formerName: FormerNameView,
                                      formerNameDate: Option[FormerNameDateView],
                                      prevAddr: PreviousAddressView): JsObject = {
    val currentAddress = Json.obj("currentAddress" ->
      Json.toJson(currAddr.address.getOrElse(throw new IllegalStateException("Missing applicant current address to save into backend")))
    )

    val email = applicantContact.email.fold(Json.obj())(v => Json.obj("email" -> v))
    val tel = applicantContact.daytimePhone.fold(Json.obj())(v => Json.obj("tel" -> v))
    val mobile = applicantContact.mobile.fold(Json.obj())(v => Json.obj("mobile" -> v))
    val contact = Json.obj("contact" -> email.++(tel).++(mobile))

    val previousAddress = if (prevAddr.yesNo) Json.obj() else {
      Json.obj("previousAddress" ->
        Json.toJson(prevAddr.address.getOrElse(throw new IllegalStateException("Missing applicant previous address to save into backend"))).as[JsObject]
      )
    }
    val changeOfName = if (!formerName.yesNo) Json.obj() else {
      val (first, middle, last) = splitName(formerName.formerName.getOrElse(
        throw new IllegalStateException("Missing applicant former name to save into backend")
      ))
      val jsonFirstName = first.fold(Json.obj())(fn => Json.obj("first" -> fn))
      val jsonMiddleName = middle.fold(Json.obj())(mn => Json.obj("middle" -> mn))
      val jsonLastName = last.fold(Json.obj())(sn => Json.obj("last" -> sn))
      val jsonFormerName = Json.obj("name" -> jsonFirstName.++(jsonMiddleName).++(jsonLastName))

      val changeDate = formerNameDate.getOrElse(throw new IllegalStateException("Missing applicant former name change date to save into backend"))
      val jsonChangeDate = Json.obj("change" -> changeDate.date)

      Json.obj("changeOfName" -> jsonFormerName.++(jsonChangeDate))
    }

    currentAddress ++ contact ++ changeOfName ++ previousAddress
  }

  def apiWrites: Writes[ApplicantDetails] = new Writes[ApplicantDetails] {
    override def writes(o: ApplicantDetails): JsObject = {

      val applicantDetails = o match {
        case ApplicantDetails(None, None, None, None, None) =>
          Json.obj()
        case ApplicantDetails(Some(currAddr), Some(contact), Some(fName), _@fNameDate, Some(prevAddr)) =>
          Json.obj(
            "nino" -> DeprecatedConstants.fakeNino,
            "name" -> DeprecatedConstants.fakeName,
            "role" -> DeprecatedConstants.fakeRole,
            "dateOfBirth" -> DeprecatedConstants.fakeDateOfBirth
          ) ++ buildJsonApplicantDetails(currAddr, contact, fName, fNameDate, prevAddr)
        case _ =>
          throw new IllegalStateException("Missing applicant details data to save into backend")
      }

      applicantDetails
    }
  }
}
