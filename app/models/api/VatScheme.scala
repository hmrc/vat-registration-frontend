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

import common.enums.VatRegStatus
import models.api.returns.Returns
import models.{ApplicantDetails, _}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class VatScheme(id: String,
                     applicantDetails: Option[ApplicantDetails] = None,
                     transactorDetails: Option[TransactorDetails] = None,
                     tradingDetails: Option[TradingDetails] = None,
                     sicAndCompliance: Option[SicAndCompliance] = None,
                     businessContact: Option[BusinessContact] = None,
                     returns: Option[Returns] = None,
                     bankAccount: Option[BankAccount] = None,
                     flatRateScheme: Option[FlatRateScheme] = None,
                     status: VatRegStatus.Value,
                     eligibilitySubmissionData: Option[EligibilitySubmissionData] = None,
                     partners: Option[List[PartnerEntity]] = None,
                     createdDate: Option[LocalDate] = None)

object VatScheme {

  val reads: Reads[VatScheme] =
    (__ \ "eligibilitySubmissionData" \ "partyType").readNullable[PartyType].flatMap {
      case Some(partyType) => (
        (__ \ "registrationId").read[String] and
          (__ \ "applicantDetails").readNullable[ApplicantDetails](ApplicantDetails.reads(partyType)) and
          (__ \ "transactorDetails").readNullable[TransactorDetails] and
          (__ \ "tradingDetails").readNullable[TradingDetails](TradingDetails.apiFormat) and
          (__ \ "sicAndCompliance").readNullable[SicAndCompliance](SicAndCompliance.apiFormat) and
          (__ \ "businessContact").readNullable[BusinessContact](BusinessContact.apiFormat) and
          (__ \ "returns").readNullable[Returns] and
          (__ \ "bankAccount").readNullable[BankAccount] and
          (__ \ "flatRateScheme").readNullable[FlatRateScheme](FlatRateScheme.apiFormat) and
          (__ \ "status").read[VatRegStatus.Value] and
          (__ \ "eligibilitySubmissionData").readNullable[EligibilitySubmissionData] and
          (__ \ "partners").readNullable[List[PartnerEntity]] and
          (__ \ "createdDate").readNullable[LocalDate]
        ) (VatScheme.apply _)
      case None => (
        (__ \ "registrationId").read[String] and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          (__ \ "status").read[VatRegStatus.Value] and
          Reads.pure(None) and
          Reads.pure(None) and
          (__ \ "createdDate").readNullable[LocalDate]
        ) (VatScheme.apply _)
    }

  val writes: Writes[VatScheme] = (
    (__ \ "registrationId").write[String] and
      (__ \ "applicantDetails").writeNullable[ApplicantDetails](ApplicantDetails.writes) and
      (__ \ "transactorDetails").writeNullable[TransactorDetails] and
      (__ \ "tradingDetails").writeNullable[TradingDetails](TradingDetails.apiFormat) and
      (__ \ "sicAndCompliance").writeNullable[SicAndCompliance](SicAndCompliance.apiFormat) and
      (__ \ "businessContact").writeNullable[BusinessContact](BusinessContact.apiFormat) and
      (__ \ "returns").writeNullable[Returns] and
      (__ \ "bankAccount").writeNullable[BankAccount] and
      (__ \ "flatRateScheme").writeNullable[FlatRateScheme](FlatRateScheme.apiFormat) and
      (__ \ "status").write[VatRegStatus.Value] and
      (__ \ "eligibilitySubmissionData").writeNullable[EligibilitySubmissionData] and
      (__ \ "partners").writeNullable[List[PartnerEntity]] and
      (__ \ "createdDate").writeNullable[LocalDate]
    ) (unlift(VatScheme.unapply))

  implicit val format: Format[VatScheme] = Format(reads, writes)

}
