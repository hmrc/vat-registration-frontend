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

case class CompanyContactDetails(email: String,
                                 phoneNumber: Option[String],
                                 mobileNumber: Option[String],
                                 website: Option[String])

object CompanyContactDetails {
  implicit val format: Format[CompanyContactDetails] = Json.format[CompanyContactDetails]

  val apiReads: Reads[CompanyContactDetails] = (
    (__ \ "email").read[String] and
    (__ \ "telephoneNumber").readNullable[String] and
    (__ \ "mobile").readNullable[String] and
    (__ \ "website").readNullable[String]
  ) (CompanyContactDetails.apply _)

  val apiWrites: Writes[CompanyContactDetails] = (companyContactDetails: CompanyContactDetails) => {
    val email = Json.obj("email" -> companyContactDetails.email)
    val telephoneNumber = companyContactDetails.phoneNumber.fold(Json.obj())(x => Json.obj("telephoneNumber" -> x))
    val mobile = companyContactDetails.mobileNumber.fold(Json.obj())(x => Json.obj("mobile" -> x))
    val website = companyContactDetails.website.fold(Json.obj())(x => Json.obj("website" -> x))

    email ++ telephoneNumber ++ mobile ++ website
  }

  val apiFormat: Format[CompanyContactDetails] = Format[CompanyContactDetails](apiReads, apiWrites)
}
