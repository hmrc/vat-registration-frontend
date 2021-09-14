/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json._

sealed trait AttachmentType {
  lazy val typeName: String = toString
}

case object LetterOfAuthority extends AttachmentType
case object VAT51 extends AttachmentType
case object VAT2 extends AttachmentType
case object Attachment1614a extends AttachmentType
case object Attachment1614h extends AttachmentType
case object VAT5L extends AttachmentType
case object LandPropertyOtherDocs extends AttachmentType
case object IdentityEvidence extends AttachmentType
case object TaxRepresentativeAuthorisation extends AttachmentType
case object TaxAgentAuthorisation extends AttachmentType
case object OtherAttachments extends AttachmentType

object AttachmentType {
  val map: Map[AttachmentType, String] = Map(
    LetterOfAuthority -> "letterOfAuthority",
    VAT51 -> "VAT51",
    VAT2 -> "VAT2",
    Attachment1614a -> "attachment1614a",
    Attachment1614h -> "attachment1614h",
    VAT5L -> "VAT5L",
    LandPropertyOtherDocs -> "landPropertyOtherDocs",
    IdentityEvidence -> "identityEvidence",
    TaxRepresentativeAuthorisation -> "taxRepresentativeAuthorisation",
    TaxAgentAuthorisation -> "taxAgentAuthorisation",
    OtherAttachments -> "otherAttachments"
  )
  val inverseMap: Map[String, AttachmentType] = map.map(_.swap)

  implicit val format: Format[AttachmentType] = Format(
    Reads[AttachmentType](json => json.validate[String].map(string => inverseMap(string))),
    Writes[AttachmentType](attachmentType => JsString(map(attachmentType)))
  )
}
