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

package models

import models.api.ScrsAddress
import play.api.libs.json._

case class BusinessContact(ppobAddress: Option[ScrsAddress] = None,
                           companyContactDetails: Option[CompanyContactDetails] = None)

object BusinessContact {
  implicit val format: Format[BusinessContact] = Json.format[BusinessContact]
  implicit val businessContactS4lKey: S4LKey[BusinessContact] = S4LKey("business-contact")

  def fromApi(json: JsValue): BusinessContact = {
    val ppob           = json.\("ppob").as[ScrsAddress]
    val contactDetails = json.as[CompanyContactDetails](CompanyContactDetails.apiReads)
    BusinessContact(
      ppobAddress           = Some(ppob),
      companyContactDetails = Some(contactDetails)
    )
  }

  def toApi(businessContact: BusinessContact): JsValue = {
    val ppob            = Json.obj("ppob" -> Json.toJson(businessContact.ppobAddress.get).as[JsObject])
    val contactDetails  = Json.toJson(businessContact.companyContactDetails.get)(CompanyContactDetails.apiWrites).as[JsObject]

    ppob ++ contactDetails
  }

  val apiFormat: Format[BusinessContact] = new Format[BusinessContact] {
    override def writes(o: BusinessContact) = toApi(o)
    override def reads(json: JsValue)       = JsSuccess(fromApi(json))
  }
}
