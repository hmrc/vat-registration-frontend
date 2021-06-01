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

import models.external.Name

import java.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class IncorporationDetails(companyNumber: String,
                                companyName: String,
                                ctutr: String,
                                dateOfIncorporation: LocalDate,
                                countryOfIncorporation: String = "GB",
                                identifiersMatch: Boolean,
                                registration: Option[String] = None,
                                businessVerification: Option[BusinessVerificationStatus] = None,
                                bpSafeId: Option[String] = None)

object IncorporationDetails {
  val apiReads: Reads[IncorporationDetails] = (
    (__ \ "companyProfile" \ "companyNumber").read[String] and
      (__ \ "companyProfile" \ "companyName").read[String] and
      (__ \ "ctutr").read[String] and
      (__ \ "companyProfile" \ "dateOfIncorporation").read[LocalDate] and
      Reads.pure("GB") and
      (__ \ "identifiersMatch").read[Boolean] and
      (__ \ "registration" \ "registrationStatus").readNullable[String].orElse(Reads.pure(None)) and
      (__ \ "businessVerification" \ "verificationStatus").readNullable[BusinessVerificationStatus].orElse(Reads.pure(None)) and
      (__ \ "registration" \ "registeredBusinessPartnerId").readNullable[String].orElse(Reads.pure(None))
    ) (IncorporationDetails.apply _)

  val apiWrites: Writes[IncorporationDetails] = (
    (__ \ "companyProfile" \ "companyNumber").write[String] and
      (__ \ "companyProfile" \ "companyName").write[String] and
      (__ \ "ctutr").write[String] and
      (__ \ "companyProfile" \ "dateOfIncorporation").write[LocalDate] and
      (__ \ "companyProfile" \ "countryOfIncorporation").write[String] and
      (__ \ "identifiersMatch").write[Boolean] and
      (__ \ "registration" \ "registrationStatus").writeNullable[String] and
      (__ \ "businessVerification" \ "verificationStatus").writeNullable[BusinessVerificationStatus] and
      (__ \ "registration" \ "registeredBusinessPartnerId").writeNullable[String]
    ) (unlift(IncorporationDetails.unapply))

  val apiFormat = Format[IncorporationDetails](apiReads, apiWrites)

  implicit val format: Format[IncorporationDetails] = Json.format[IncorporationDetails]
}