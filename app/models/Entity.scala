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

import models.api.{Address, PartyType, ScotPartnership}
import models.external.{BusinessEntity, IncorporatedEntity, PartnershipIdEntity, SoleTraderIdEntity}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Entity(details: Option[BusinessEntity],
                  partyType: PartyType,
                  isLeadPartner: Option[Boolean],
                  optScottishPartnershipName: Option[String],
                  address: Option[Address],
                  email: Option[String],
                  telephoneNumber: Option[String]) {
  def displayName: Option[String] = details match {
    case Some(soleTrader: SoleTraderIdEntity) => Some(soleTrader.firstName)
    case Some(incorpBusiness: IncorporatedEntity) => incorpBusiness.companyName
    case Some(partnershipBusiness: PartnershipIdEntity) => partnershipBusiness.companyName
    case _ => None
  }
}

object Entity {
  val leadEntityIndex = 1
  val reads: Reads[Entity] =
    (__ \ "partyType").read[PartyType].flatMap { partyType =>
      (
        (__ \ "details").readNullable[BusinessEntity](BusinessEntity.reads(partyType)) and
        (__ \ "isLeadPartner").readNullable[Boolean] and
        (__ \ "optScottishPartnershipName").readNullable[String] and
        (__ \ "address").readNullable[Address] and
        (__ \ "email").readNullable[String] and
        (__ \ "telephoneNumber").readNullable[String]
      ) { (optDetails, optIsLeadPartner, optScottishPartnershipName, address, email, telephoneNumber) =>
        val updatedDetails = optDetails.map {
          case details: PartnershipIdEntity if partyType.equals(ScotPartnership) => details.copy(companyName = optScottishPartnershipName)
          case notScottishPartnership => notScottishPartnership
        }

        Entity(
          updatedDetails,
          partyType,
          optIsLeadPartner,
          optScottishPartnershipName,
          address,
          email,
          telephoneNumber
        )
      }
    }

  val writes: OWrites[Entity] = Json.writes[Entity]

  implicit val apiKey: ApiKey[Entity] = ApiKey("entities")
  implicit val format: Format[Entity] = Format(reads, writes)
}
