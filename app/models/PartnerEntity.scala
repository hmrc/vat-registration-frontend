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

package models

import models.api.PartyType
import models.external.BusinessEntity
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PartnerEntity(details: BusinessEntity,
                         partyType: PartyType,
                         isLeadPartner: Boolean)

object PartnerEntity {
  val reads: Reads[PartnerEntity] =
    (__ \ "partyType").read[PartyType].flatMap { partyType =>
      (
        (__ \ "details").read[BusinessEntity](BusinessEntity.reads(partyType)) and
        Reads.pure(partyType) and
        (__ \ "isLeadPartner").read[Boolean]
      )(PartnerEntity.apply _)
    }

  val writes: OWrites[PartnerEntity] = Json.writes[PartnerEntity]

  implicit val format: Format[PartnerEntity] = Format(reads, writes)

}
