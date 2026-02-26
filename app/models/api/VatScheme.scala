/*
 * Copyright 2026 HM Revenue & Customs
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

case class VatScheme(registrationId: String,
                     createdDate: LocalDate,
                     status: VatRegStatus.Value,
                     applicationReference: Option[String] = None,
                     confirmInformationDeclaration: Option[Boolean] = None,
                     eligibilityJson: Option[JsObject] = None,
                     eligibilitySubmissionData: Option[EligibilitySubmissionData] = None,
                     transactorDetails: Option[TransactorDetails] = None,
                     applicantDetails: Option[ApplicantDetails] = None,
                     entities: Option[List[Entity]] = None,
                     business: Option[Business] = None,
                     otherBusinessInvolvements: Option[List[OtherBusinessInvolvement]] = None,
                     vatApplication: Option[VatApplication] = None,
                     bankAccount: Option[BankAccount] = None,
                     flatRateScheme: Option[FlatRateScheme] = None,
                     attachments: Option[Attachments] = None) {

  def partyType: Option[PartyType] = eligibilitySubmissionData.map(_.partyType)
  def registrationReason: Option[RegistrationReason] = eligibilitySubmissionData.map(_.registrationReason)
}

object VatScheme {

  val reads: Reads[VatScheme] =
    (__ \ "eligibilitySubmissionData" \ "partyType").readNullable[PartyType].flatMap {
      case Some(partyType) => (
        (__ \ "registrationId").read[String] and
        (__ \ "createdDate").read[LocalDate] and
        (__ \ "status").read[VatRegStatus.Value] and
        (__ \ "applicationReference").readNullable[String] and
        (__ \ "confirmInformationDeclaration").readNullable[Boolean] and
        (__ \ "eligibilityJson").readNullable[JsObject] and
        (__ \ "eligibilitySubmissionData").readNullable[EligibilitySubmissionData] and
        (__ \ "transactorDetails").readNullable[TransactorDetails] and
        (__ \ "applicantDetails").readNullable[ApplicantDetails](ApplicantDetails.reads(partyType)) and
        (__ \ "entities").readNullable[List[Entity]] and
        (__ \ "business").readNullable[Business] and
        (__ \ "otherBusinessInvolvements").readNullable[List[OtherBusinessInvolvement]] and
        (__ \ "vatApplication").readNullable[VatApplication] and
        (__ \ "bankAccount").readNullable[BankAccount] and
        (__ \ "flatRateScheme").readNullable[FlatRateScheme] and
        (__ \ "attachments").readNullable[Attachments]
      ) (VatScheme.apply _)
      case None => (
        (__ \ "registrationId").read[String] and
        (__ \ "createdDate").read[LocalDate] and
        (__ \ "status").read[VatRegStatus.Value] and
        (__ \ "applicationReference").readNullable[String] and
        (__ \ "confirmInformationDeclaration").readNullable[Boolean] and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None)
      ) (VatScheme.apply _)
    }

  val writes: Writes[VatScheme] = (
    (__ \ "registrationId").write[String] and
    (__ \ "createdDate").write[LocalDate] and
    (__ \ "status").write[VatRegStatus.Value] and
    (__ \ "applicationReference").writeNullable[String] and
    (__ \ "confirmInformationDeclaration").writeNullable[Boolean] and
    (__ \ "eligibilityJson").writeNullable[JsObject] and
    (__ \ "eligibilitySubmissionData").writeNullable[EligibilitySubmissionData] and
    (__ \ "transactorDetails").writeNullable[TransactorDetails] and
    (__ \ "applicantDetails").writeNullable[ApplicantDetails](ApplicantDetails.writes) and
    (__ \ "entities").writeNullable[List[Entity]] and
    (__ \ "business").writeNullable[Business] and
    (__ \ "otherBusinessInvolvements").writeNullable[List[OtherBusinessInvolvement]] and
    (__ \ "vatApplication").writeNullable[VatApplication] and
    (__ \ "bankAccount").writeNullable[BankAccount] and
    (__ \ "flatRateScheme").writeNullable[FlatRateScheme] and
    (__ \ "attachments").writeNullable[Attachments]
  ) (unlift(VatScheme.unapply))

  implicit val format: Format[VatScheme] = Format(reads, writes)

}
