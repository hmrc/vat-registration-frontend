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

import common.enums.VatRegStatus
import models._
import models.api.vatapplication.VatApplication
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class VatScheme(id: String,
                     applicantDetails: Option[ApplicantDetails] = None,
                     transactorDetails: Option[TransactorDetails] = None,
                     vatApplication: Option[VatApplication] = None,
                     bankAccount: Option[BankAccount] = None,
                     flatRateScheme: Option[FlatRateScheme] = None,
                     status: VatRegStatus.Value,
                     eligibilitySubmissionData: Option[EligibilitySubmissionData] = None,
                     partners: Option[List[PartnerEntity]] = None,
                     createdDate: Option[LocalDate] = None,
                     applicationReference: Option[String] = None,
                     otherBusinessInvolvements: Option[List[OtherBusinessInvolvement]] = None,
                     business: Option[Business] = None) {

  def partyType: Option[PartyType] = eligibilitySubmissionData.map(_.partyType)

}

object VatScheme {

  val reads: Reads[VatScheme] =
    (__ \ "eligibilitySubmissionData" \ "partyType").readNullable[PartyType].flatMap {
      case Some(partyType) => (
        (__ \ "registrationId").read[String] and
          (__ \ "applicantDetails").readNullable[ApplicantDetails](ApplicantDetails.reads(partyType)) and
          (__ \ "transactorDetails").readNullable[TransactorDetails] and
          (__ \ "vatApplication").readNullable[VatApplication] and
          (__ \ "bankAccount").readNullable[BankAccount] and
          (__ \ "flatRateScheme").readNullable[FlatRateScheme](FlatRateScheme.apiFormat) and
          (__ \ "status").read[VatRegStatus.Value] and
          (__ \ "eligibilitySubmissionData").readNullable[EligibilitySubmissionData] and
          (__ \ "partners").readNullable[List[PartnerEntity]] and
          (__ \ "createdDate").readNullable[LocalDate] and
          (__ \ "applicationReference").readNullable[String] and
          (__ \ "otherBusinessInvolvements").readNullable[List[OtherBusinessInvolvement]] and
          (__ \ "business").readNullable[Business]
        ) (VatScheme.apply _)
      case None => (
        (__ \ "registrationId").read[String] and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          Reads.pure(None) and
          (__ \ "status").read[VatRegStatus.Value] and
          Reads.pure(None) and
          Reads.pure(None) and
          (__ \ "createdDate").readNullable[LocalDate] and
          (__ \ "applicationReference").readNullable[String] and
          (__ \ "otherBusinessInvolvements").readNullable[List[OtherBusinessInvolvement]] and
          Reads.pure(None)
        ) (VatScheme.apply _)
    }

  val writes: Writes[VatScheme] = (
    (__ \ "registrationId").write[String] and
      (__ \ "applicantDetails").writeNullable[ApplicantDetails](ApplicantDetails.writes) and
      (__ \ "transactorDetails").writeNullable[TransactorDetails] and
      (__ \ "vatApplication").writeNullable[VatApplication] and
      (__ \ "bankAccount").writeNullable[BankAccount] and
      (__ \ "flatRateScheme").writeNullable[FlatRateScheme](FlatRateScheme.apiFormat) and
      (__ \ "status").write[VatRegStatus.Value] and
      (__ \ "eligibilitySubmissionData").writeNullable[EligibilitySubmissionData] and
      (__ \ "partners").writeNullable[List[PartnerEntity]] and
      (__ \ "createdDate").writeNullable[LocalDate] and
      (__ \ "applicationReference").writeNullable[String] and
      (__ \ "otherBusinessInvolvements").writeNullable[List[OtherBusinessInvolvement]] and
      (__ \ "business").writeNullable[Business]
    ) (unlift(VatScheme.unapply))

  implicit val format: Format[VatScheme] = Format(reads, writes)

}
