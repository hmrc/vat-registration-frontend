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

package models.api

import common.enums.VatRegStatus
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class VatSchemeHeader(registrationId: String,
                           status: VatRegStatus.Value,
                           applicationReference: Option[String] = None,
                           createdDate: Option[LocalDate] = None,
                           requiresAttachments: Boolean)

case object VatSchemeHeader {

  val reads: Reads[VatSchemeHeader] = (
    (__ \ "registrationId").read[String] and
    (__ \ "status").read[VatRegStatus.Value] and
    (__ \ "applicationReference").readNullable[String] and
    (__ \ "createdDate").readNullable[LocalDate] and
    (__ \ "attachments").readNullable[JsValue].fmap(block => block.isDefined)
  )(VatSchemeHeader.apply _)

  val writes = Json.format[VatSchemeHeader]

  implicit val format: Format[VatSchemeHeader] = Format[VatSchemeHeader](reads, writes)
}
