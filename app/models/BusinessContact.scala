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

import models.api.Address
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class BusinessContact(ppobAddress: Option[Address] = None,
                           email: Option[String] = None,
                           telephoneNumber: Option[String] = None,
                           hasWebsite: Option[Boolean] = None,
                           website: Option[String] = None,
                           contactPreference: Option[ContactPreference] = None)

object BusinessContact {
  implicit val format: Format[BusinessContact] = Json.format[BusinessContact]
  implicit val apiKey: ApiKey[BusinessContact] = ApiKey("business-contact")
  implicit val s4lKey: S4LKey[BusinessContact] = S4LKey("business-contact")

  val apiReads: Reads[BusinessContact] = (
    (__ \ "ppob").readNullable[Address] and
      (__ \ "email").readNullable[String] and
      (__ \ "telephoneNumber").readNullable[String] and
      (__ \ "hasWebsite").readNullable[Boolean] and
      (__ \ "website").readNullable[String] and
      (__ \ "contactPreference").readNullable[ContactPreference]
    ) (BusinessContact.apply _)

  val apiWrites: Writes[BusinessContact] = (
      (__ \ "ppob").writeNullable[Address] and
        (__ \ "email").writeNullable[String] and
        (__ \ "telephoneNumber").writeNullable[String] and
        (__ \ "hasWebsite").writeNullable[Boolean] and
        (__ \ "website").writeOptionWithNull[String] and
        (__ \ "contactPreference").writeNullable[ContactPreference]
      ) (unlift(BusinessContact.unapply))

  val apiFormat = Format[BusinessContact](apiReads, apiWrites)
}
