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
import play.api.libs.json._

case class LodgingOfficer(homeAddress: Option[HomeAddressView],
                          contactDetails: Option[ContactDetailsView],
                          formerName: Option[FormerNameView],
                          formerNameDate: Option[FormerNameDateView],
                          previousAddress: Option[PreviousAddressView])

object LodgingOfficer {
  implicit val format: Format[LodgingOfficer] = Json.format[LodgingOfficer]
  implicit val s4lKey: S4LKey[LodgingOfficer] = S4LKey("LodgingOfficer")

  def fromJsonToName(json: JsValue): Name = Name(
    forename = (json \ "name" \ "first").validateOpt[String].get,
    otherForenames = (json \ "name" \ "middle").validateOpt[String].get,
    surname = (json \ "name" \ "last").validate[String].get,
    title = None
  )

  def fromApi(lodgingOfficer: JsValue): LodgingOfficer = {

    val lodgingOfficerView = LodgingOfficer(
      homeAddress = None,
      contactDetails = None,
      formerName = None,
      formerNameDate = None,
      previousAddress = None
    )

    val detailsBase = (lodgingOfficer \ "details").validateOpt[JsObject].get
    detailsBase.fold(lodgingOfficerView) { details =>
      val currentAddress: ScrsAddress = (details \ "currentAddress").as[ScrsAddress]
      val homeAddress = HomeAddressView(currentAddress.id, Some(currentAddress))

      val digitalContact = ContactDetailsView(
        email = (details \ "contact" \ "email").validateOpt[String].get,
        daytimePhone = (details \ "contact" \ "tel").validateOpt[String].get,
        mobile = (details \ "contact" \ "mobile").validateOpt[String].get
      )

      val changeOfName: Option[JsObject] = (details \ "changeOfName").validateOpt[JsObject].get
      val formerNameView: FormerNameView = FormerNameView(changeOfName.isDefined, changeOfName.map(fromJsonToName(_).asLabel))
      val formerNameDateView: Option[FormerNameDateView] = changeOfName.map(json => FormerNameDateView((json \ "change").as[LocalDate]))

      val previousAddress: Option[ScrsAddress] = (details \ "previousAddress").validateOpt[ScrsAddress].get

      lodgingOfficerView.copy(
        homeAddress = Some(homeAddress),
        contactDetails = Some(digitalContact),
        formerName = Some(formerNameView),
        formerNameDate = formerNameDateView,
        previousAddress = Some(PreviousAddressView(previousAddress.isEmpty, previousAddress))
      )
    }
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

  private def buildJsonOfficerDetails(currAddr: HomeAddressView,
                                      officerContact: ContactDetailsView,
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

    val previousAddress = if (prevAddr.yesNo) Json.obj() else {
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

  def apiWrites: Writes[LodgingOfficer] = new Writes[LodgingOfficer] {
    override def writes(o: LodgingOfficer): JsObject = {

      val officerDetails = o match {
        case LodgingOfficer(None, None, None, None, None) =>
          Json.obj()
        case LodgingOfficer(Some(currAddr), Some(contact), Some(fName), _@fNameDate, Some(prevAddr)) =>
          Json.obj("details" -> buildJsonOfficerDetails(currAddr, contact, fName, fNameDate, prevAddr))
        case _ =>
          throw new IllegalStateException("Missing officer details data to save into backend")
      }

      officerDetails
    }
  }
}
