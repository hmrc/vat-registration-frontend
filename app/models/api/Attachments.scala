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

import models.ApiKey
import play.api.libs.json._


case class Attachments(method: Option[AttachmentMethod] = None,
                       supplyVat1614a: Option[Boolean] = None,
                       supplyVat1614h: Option[Boolean] = None,
                       supplySupportingDocuments: Option[Boolean] = None)

object Attachments {
  implicit val apiKey: ApiKey[Attachments] = ApiKey("attachments")
  implicit val format: Format[Attachments] = Json.format[Attachments]
}

sealed trait AttachmentMethod

case object OtherAttachmentMethod extends AttachmentMethod

case object Attached extends AttachmentMethod

case object Post extends AttachmentMethod

case object EmailMethod extends AttachmentMethod

object AttachmentMethod {
  val map: Map[AttachmentMethod, String] = Map(
    OtherAttachmentMethod -> "1",
    Attached -> "2",
    Post -> "3",
    EmailMethod -> "email"
  )
  val inverseMap: Map[String, AttachmentMethod] = map.map(_.swap)

  implicit val format: Format[AttachmentMethod] = Format(
    Reads[AttachmentMethod](json => json.validate[String].map(string => inverseMap(string))),
    Writes[AttachmentMethod](attachmentOption => JsString(map(attachmentOption)))
  )
}
