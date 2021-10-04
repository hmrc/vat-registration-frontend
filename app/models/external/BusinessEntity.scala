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

package models.external

import models.api._
import models.external.soletraderid.OverseasIdentifierDetails
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException

import java.time.LocalDate

sealed trait BusinessEntity

object BusinessEntity {
  def reads(partyType: PartyType): Reads[BusinessEntity] = Reads { json =>
    partyType match {
      case UkCompany | RegSociety | CharitableOrg => Json.fromJson(json)(IncorporatedEntity.format)
      case Individual | NETP => Json.fromJson(json)(SoleTraderIdEntity.format)
      case Partnership => Json.fromJson(json)(PartnershipIdEntity.format)
      case UnincorpAssoc | Trust => Json.fromJson(json)(BusinessIdEntity.format)
      case _ => throw new InternalServerException("Tried to parse business entity for an unsupported party type")
    }
  }

  implicit val writes: Writes[BusinessEntity] = Writes {
    case incorporatedEntity: IncorporatedEntity => Json.toJson(incorporatedEntity)
    case soleTrader: SoleTraderIdEntity => Json.toJson(soleTrader)
    case partnershipIdEntity: PartnershipIdEntity => Json.toJson(partnershipIdEntity)
    case businessIdEntity: BusinessIdEntity => Json.toJson(businessIdEntity)
  }
}

case class IncorporatedEntity(companyNumber: String,
                              companyName: String,
                              ctutr: Option[String] = None,
                              chrn: Option[String] = None,
                              dateOfIncorporation: LocalDate,
                              countryOfIncorporation: String = "GB",
                              identifiersMatch: Boolean,
                              registration: String,
                              businessVerification: BusinessVerificationStatus,
                              bpSafeId: Option[String]) extends BusinessEntity

object IncorporatedEntity {
  val apiFormat: Format[IncorporatedEntity] = (
    (__ \ "companyProfile" \ "companyNumber").format[String] and
      (__ \ "companyProfile" \ "companyName").format[String] and
      (__ \ "ctutr").formatNullable[String] and
      (__ \ "chrn").formatNullable[String] and
      (__ \ "companyProfile" \ "dateOfIncorporation").format[LocalDate] and
      OFormat(Reads.pure("GB"), (__ \ "companyProfile" \ "countryOfIncorporation").write[String]) and
      (__ \ "identifiersMatch").format[Boolean] and
      (__ \ "registration" \ "registrationStatus").format[String] and
      (__ \ "businessVerification" \ "verificationStatus").format[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").formatNullable[String]
    ) (IncorporatedEntity.apply, unlift(IncorporatedEntity.unapply))

  implicit val format: Format[IncorporatedEntity] = Json.format[IncorporatedEntity]
}

case class SoleTraderIdEntity(firstName: String,
                              lastName: String,
                              dateOfBirth: LocalDate,
                              nino: Option[String],
                              sautr: Option[String],
                              trn: Option[String],
                              registration: String,
                              businessVerification: BusinessVerificationStatus,
                              bpSafeId: Option[String] = None,
                              overseas: Option[OverseasIdentifierDetails] = None,
                              identifiersMatch: Boolean = true) extends BusinessEntity

object SoleTraderIdEntity {
  val apiFormat: Format[SoleTraderIdEntity] = (
    (__ \ "fullName" \ "firstName").format[String] and
      (__ \ "fullName" \ "lastName").format[String] and
      (__ \ "dateOfBirth").format[LocalDate] and
      (__ \ "nino").formatNullable[String] and
      (__ \ "sautr").formatNullable[String] and
      (__ \ "trn").formatNullable[String] and
      (__ \ "registration" \ "registrationStatus").format[String] and
      (__ \ "businessVerification" \ "verificationStatus").format[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").formatNullable[String] and
      (__ \ "overseas").formatNullable[OverseasIdentifierDetails] and
      OFormat((__ \ "identifiersMatch").read[Boolean].orElse(Reads.pure(true)), (__ \ "identifiersMatch").write[Boolean])
    ) (SoleTraderIdEntity.apply, unlift(SoleTraderIdEntity.unapply))

  implicit val format: Format[SoleTraderIdEntity] = Json.format[SoleTraderIdEntity]
}

case class PartnershipIdEntity(sautr: Option[String],
                               postCode: Option[String],
                               registration: String,
                               businessVerification: BusinessVerificationStatus,
                               bpSafeId: Option[String] = None,
                               identifiersMatch: Boolean) extends BusinessEntity

object PartnershipIdEntity {

  val apiFormat: Format[PartnershipIdEntity] = (
    (__ \ "sautr").formatNullable[String] and
      (__ \ "postcode").formatNullable[String] and
      (__ \ "registration" \ "registrationStatus").format[String] and
      (__ \ "businessVerification" \ "verificationStatus").format[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").formatNullable[String] and
      (__ \ "identifiersMatch").format[Boolean]
    ) (PartnershipIdEntity.apply, unlift(PartnershipIdEntity.unapply))

  implicit val format: Format[PartnershipIdEntity] = Json.format[PartnershipIdEntity]

}

case class BusinessIdEntity(sautr: Option[String],
                            postCode: Option[String],
                            chrn: Option[String],
                            casc: Option[String],
                            registration: String,
                            businessVerification: BusinessVerificationStatus,
                            bpSafeId: Option[String] = None,
                            identifiersMatch: Boolean) extends BusinessEntity

object BusinessIdEntity {

  val apiFormat: Format[BusinessIdEntity] = (
    (__ \ "sautr").formatNullable[String] and
      (__ \ "postcode").formatNullable[String] and
      (__ \ "chrn").formatNullable[String] and
      (__ \ "casc").formatNullable[String] and
      (__ \ "registration" \ "registrationStatus").format[String] and
      (__ \ "businessVerification" \ "verificationStatus").format[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").formatNullable[String] and
      (__ \ "identifiersMatch").format[Boolean]
    ) (BusinessIdEntity.apply, unlift(BusinessIdEntity.unapply))

  implicit val format: Format[BusinessIdEntity] = Json.format[BusinessIdEntity]

}

