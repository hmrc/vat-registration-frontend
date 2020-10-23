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

import models.api.Address
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class BusinessContact(ppobAddress: Option[Address] = None,
                           companyContactDetails: Option[CompanyContactDetails] = None,
                           contactPreference: Option[ContactPreference] = None)

object BusinessContact {
  implicit val format: Format[BusinessContact] = Json.format[BusinessContact]
  implicit val businessContactS4lKey: S4LKey[BusinessContact] = S4LKey("business-contact")

  val apiReads: Reads[BusinessContact] = (
    (__ \ "ppob").readNullable[Address] and
      (__).readNullable[CompanyContactDetails](CompanyContactDetails.apiFormat).orElse(Reads.pure(None)) and
      (__ \ "contactPreference").readNullable[ContactPreference]
    ) (BusinessContact.apply _)

  val apiWrites: Writes[BusinessContact] = (
    (__ \ "ppob").writeNullable[Address] and
      (__).writeNullable[CompanyContactDetails](CompanyContactDetails.apiFormat) and
      (__ \ "contactPreference").writeNullable[ContactPreference]
    ) (unlift(BusinessContact.unapply))

  val apiFormat = Format[BusinessContact](apiReads, apiWrites)
}
