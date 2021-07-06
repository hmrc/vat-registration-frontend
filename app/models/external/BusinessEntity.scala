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

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait BusinessEntity

object BusinessEntity {
  val reads: Reads[BusinessEntity] = Reads { json =>
    Json.fromJson(json)(LimitedCompany.format).orElse(Json.fromJson(json)(SoleTrader.format)).orElse(Json.fromJson(json)(GeneralPartnership.format))
  }

  val writes: Writes[BusinessEntity] = Writes {
    case limitedCompany: LimitedCompany => Json.toJson(limitedCompany)
    case soleTrader: SoleTrader => Json.toJson(soleTrader)
    case generalPartnership: GeneralPartnership => Json.toJson(generalPartnership)
  }

  implicit val format: Format[BusinessEntity] = Format[BusinessEntity](reads, writes)
}

case class LimitedCompany(companyNumber: String,
                          companyName: String,
                          ctutr: String,
                          dateOfIncorporation: LocalDate,
                          countryOfIncorporation: String = "GB",
                          identifiersMatch: Boolean,
                          registration: Option[String] = None,
                          businessVerification: Option[BusinessVerificationStatus] = None,
                          bpSafeId: Option[String] = None) extends BusinessEntity

object LimitedCompany {
  val apiReads: Reads[LimitedCompany] = (
    (__ \ "companyProfile" \ "companyNumber").read[String] and
      (__ \ "companyProfile" \ "companyName").read[String] and
      (__ \ "ctutr").read[String] and
      (__ \ "companyProfile" \ "dateOfIncorporation").read[LocalDate] and
      Reads.pure("GB") and
      (__ \ "identifiersMatch").read[Boolean] and
      (__ \ "registration" \ "registrationStatus").readNullable[String].orElse(Reads.pure(None)) and
      (__ \ "businessVerification" \ "verificationStatus").readNullable[BusinessVerificationStatus].orElse(Reads.pure(None)) and
      (__ \ "registration" \ "registeredBusinessPartnerId").readNullable[String].orElse(Reads.pure(None))
    ) (LimitedCompany.apply _)

  val apiWrites: Writes[LimitedCompany] = (
    (__ \ "companyProfile" \ "companyNumber").write[String] and
      (__ \ "companyProfile" \ "companyName").write[String] and
      (__ \ "ctutr").write[String] and
      (__ \ "companyProfile" \ "dateOfIncorporation").write[LocalDate] and
      (__ \ "companyProfile" \ "countryOfIncorporation").write[String] and
      (__ \ "identifiersMatch").write[Boolean] and
      (__ \ "registration" \ "registrationStatus").writeNullable[String] and
      (__ \ "businessVerification" \ "verificationStatus").writeNullable[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").writeNullable[String]
    ) (unlift(LimitedCompany.unapply))

  val apiFormat: Format[LimitedCompany] = Format[LimitedCompany](apiReads, apiWrites)

  implicit val format: Format[LimitedCompany] = Json.format[LimitedCompany]
}

case class SoleTrader(firstName: String,
                      lastName: String,
                      dateOfBirth: LocalDate,
                      nino: String,
                      sautr: Option[String],
                      registration: String,
                      businessVerification: BusinessVerificationStatus,
                      bpSafeId: Option[String] = None,
                      identifiersMatch: Boolean) extends BusinessEntity

object SoleTrader {
  val apiReads: Reads[SoleTrader] = (
    (__ \ "fullName" \ "firstName").read[String] and
      (__ \ "fullName" \ "lastName").read[String] and
      (__ \ "dateOfBirth").read[LocalDate] and
      (__ \ "nino").read[String] and
      (__ \ "sautr").readNullable[String] and
      (__ \ "registration" \ "registrationStatus").read[String] and
      (__ \ "businessVerification" \ "verificationStatus").read[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").readNullable[String].orElse(Reads.pure(None)) and
      (__ \ "identifiersMatch").read[Boolean].orElse(Reads.pure(true))
    ) (SoleTrader.apply _)

  val apiWrites: Writes[SoleTrader] = (
    (__ \ "fullName" \ "firstName").write[String] and
      (__ \ "fullName" \ "lastName").write[String] and
      (__ \ "dateOfBirth").write[LocalDate] and
      (__ \ "nino").write[String] and
      (__ \ "sautr").writeNullable[String] and
      (__ \ "registration" \ "registrationStatus").write[String] and
      (__ \ "businessVerification" \ "verificationStatus").write[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").writeNullable[String] and
      (__ \ "identifiersMatch").write[Boolean]
    ) (unlift(SoleTrader.unapply))

  val apiFormat: Format[SoleTrader] = Format[SoleTrader](apiReads, apiWrites)

  implicit val format: Format[SoleTrader] = Json.format[SoleTrader]
}

case class GeneralPartnership(sautr: Option[String],
                              postCode: Option[String],
                              registration: String,
                              businessVerification: BusinessVerificationStatus,
                              bpSafeId: Option[String] = None,
                              identifiersMatch: Boolean) extends BusinessEntity

object GeneralPartnership {

  val apiFormat: Format[GeneralPartnership] = (
    (__ \ "sautr").formatNullable[String] and
      (__ \ "postcode").formatNullable[String] and
      (__ \ "registration" \ "registrationStatus").format[String] and
      (__ \ "businessVerification" \ "verificationStatus").format[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").formatNullable[String] and
      (__ \ "identifiersMatch").format[Boolean]
    ) (GeneralPartnership.apply, unlift(GeneralPartnership.unapply))

  implicit val format: Format[GeneralPartnership] = Json.format[GeneralPartnership]

}