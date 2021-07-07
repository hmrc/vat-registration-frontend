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

import play.api.libs.json.{Format, JsString, JsSuccess, Reads, Writes}
import uk.gov.hmrc.http.InternalServerException

sealed trait PartyType
case object UkCompany extends PartyType
case object NonUkEstablished extends PartyType
case object LtdLiabilityPartnership extends PartyType
case object CharitableOrg extends PartyType
case object RegSociety extends PartyType
case object NonUkNonEstablished extends PartyType
case object GovOrg extends PartyType
case object CorpSole extends PartyType
case object ScotPartnership extends PartyType
case object ScotLtdPartnership extends PartyType
case object Trust extends PartyType
case object Partnership extends PartyType
case object LtdPartnership extends PartyType
case object UnincorpAssoc extends PartyType
case object TaxGroups extends PartyType
case object AdminDivision extends PartyType
case object Individual extends PartyType
case object Invalid extends PartyType

object PartyType {

  val stati: Map[PartyType, String] = Map(
    UkCompany -> "50",
    NonUkEstablished -> "51",
    LtdLiabilityPartnership -> "52",
    CharitableOrg -> "53",
    RegSociety -> "54",
    NonUkNonEstablished -> "55",
    GovOrg -> "56",
    CorpSole -> "57",
    ScotPartnership -> "58",
    ScotLtdPartnership -> "59",
    Trust -> "60",
    Partnership -> "61",
    LtdPartnership -> "62",
    UnincorpAssoc -> "63",
    TaxGroups -> "64",
    AdminDivision -> "65",
    Individual -> "Z1"
  )

  val inverseStati = stati.map(_.swap).withDefaultValue(Invalid)

  def fromString(value: String): PartyType = inverseStati(value)
  def toJsString(value: PartyType): JsString = JsString(stati(value))

  implicit val writes = Writes[PartyType] { partyType => toJsString(partyType) }
  implicit val reads = Reads[PartyType] { partyType => partyType.validate[String] map fromString }
  implicit val format = Format[PartyType](reads, writes)

  val eligibilityDataJsonReads: Reads[PartyType] = Reads[PartyType] { json =>
    (json \ "businessEntity-value").validate[String].map {
      case "uk-company" => UkCompany
      case "sole-trader" => Individual
      case otherEntity => throw new InternalServerException(s"Entity type $otherEntity not currently supported")
    }
  }

}