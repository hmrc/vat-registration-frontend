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

package models.external.incorporatedentityid

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait BusinessEntity

object BusinessEntity {
  val reads: Reads[BusinessEntity] = Reads { json =>
    Json.fromJson(json)(LimitedCompany.format).orElse(Json.fromJson(json)(SoleTrader.format))
  }

  val writes: Writes[BusinessEntity] = Writes {
    case limitedCompany: LimitedCompany => Json.toJson(limitedCompany)
    case soleTrader: SoleTrader => Json.toJson(soleTrader)
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

case class SoleTrader(sautr: String,
                      registration: Option[String] = None,
                      businessVerification: Option[BusinessVerificationStatus] = None,
                      bpSafeId: Option[String] = None,
                      identifiersMatch: Boolean) extends BusinessEntity

object SoleTrader {
  val apiReads: Reads[SoleTrader] = (
    (__ \ "sautr").read[String] and
      (__ \ "registration" \ "registrationStatus").readNullable[String].orElse(Reads.pure(None)) and
      (__ \ "businessVerification" \ "verificationStatus").readNullable[BusinessVerificationStatus].orElse(Reads.pure(None)) and
      (__ \ "registration" \ "registeredBusinessPartnerId").readNullable[String].orElse(Reads.pure(None)) and
      (__ \ "identifiersMatch").read[Boolean].orElse(Reads.pure(true))
    ) (SoleTrader.apply _)

  val apiWrites: Writes[SoleTrader] = (
    (__ \ "sautr").write[String] and
      (__ \ "registration" \ "registrationStatus").writeNullable[String] and
      (__ \ "businessVerification" \ "verificationStatus").writeNullable[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").writeNullable[String] and
      (__ \ "identifiersMatch").write[Boolean]
    ) (unlift(SoleTrader.unapply))

  val apiFormat: Format[SoleTrader] = Format[SoleTrader](apiReads, apiWrites)

  implicit val format: Format[SoleTrader] = Json.format[SoleTrader]
}