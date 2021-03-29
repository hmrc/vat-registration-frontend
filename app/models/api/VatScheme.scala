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
import models.view.ApplicantDetails
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VatScheme(id: String,
                     applicantDetails: Option[ApplicantDetails] = None,
                     tradingDetails: Option[TradingDetails] = None,
                     sicAndCompliance: Option[SicAndCompliance] = None,
                     businessContact: Option[BusinessContact] = None,
                     returns: Option[Returns] = None,
                     bankAccount : Option[BankAccount] = None,
                     flatRateScheme: Option[FlatRateScheme] = None,
                     status: VatRegStatus.Value,
                     eligibilitySubmissionData: Option[EligibilitySubmissionData] = None)

object VatScheme {
  val s4lFormat: Format[VatScheme] = (
    (__ \ "registrationId").format[String] and
    (__ \ ApplicantDetails.s4lKey.key).formatNullable[ApplicantDetails] and
    (__ \ TradingDetails.s4lKey.key).formatNullable[TradingDetails] and
    (__ \ SicAndCompliance.s4lKey.key).formatNullable[SicAndCompliance]
      .inmap[Option[SicAndCompliance]](_ => Option.empty[SicAndCompliance], _ => Option.empty[SicAndCompliance]) and
    (__ \ BusinessContact.s4lKey.key).formatNullable[BusinessContact] and
    (__ \ Returns.s4lKey.key).formatNullable[Returns] and
    (__ \ BankAccount.s4lKey.key).formatNullable[BankAccount] and
    (__ \ FlatRateScheme.s4lKey.key).formatNullable[FlatRateScheme] and
    (__ \ "status").format[VatRegStatus.Value] and
    (__ \ "eligibilitySubmissionData").formatNullable[EligibilitySubmissionData]
  )(VatScheme.apply, unlift(VatScheme.unapply))

  implicit val format: OFormat[VatScheme] = (
      (__ \ "registrationId").format[String] and
      (__ \ "applicantDetails").formatNullable[ApplicantDetails](ApplicantDetails.apiFormat) and
      (__ \ "tradingDetails").formatNullable[TradingDetails](TradingDetails.apiFormat) and
      (__ \ "sicAndCompliance").formatNullable[SicAndCompliance](SicAndCompliance.apiFormat)
        .inmap[Option[SicAndCompliance]](_ => Option.empty[SicAndCompliance], _ => Option.empty[SicAndCompliance]) and
      (__ \ "businessContact").formatNullable[BusinessContact](BusinessContact.apiFormat) and
      (__ \ "returns").formatNullable[Returns] and
      (__ \ "bankAccount").formatNullable[BankAccount] and
      (__ \ "flatRateScheme").formatNullable[FlatRateScheme](FlatRateScheme.apiFormat) and
      (__ \ "status").format[VatRegStatus.Value] and
      (__ \ "eligibilitySubmissionData").formatNullable[EligibilitySubmissionData]
    )(VatScheme.apply, unlift(VatScheme.unapply))
}
