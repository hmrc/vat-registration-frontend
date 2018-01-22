/*
 * Copyright 2018 HM Revenue & Customs
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
import features.officer.models.view.LodgingOfficer
import features.returns.Returns
import features.tradingDetails.TradingDetails
import features.turnoverEstimates.TurnoverEstimates
import models.BankAccount
import play.api.libs.functional.syntax._
import play.api.libs.json._


case class VatScheme(
                      id: String,
                      lodgingOfficer: Option[LodgingOfficer] = None,
                      tradingDetails: Option[TradingDetails] = None,
                      financials: Option[VatFinancials] = None,
                      vatSicAndCompliance: Option[VatSicAndCompliance] = None,
                      vatContact: Option[VatContact] = None,
                      threshold: Option[Threshold] = None,
                      returns: Option[Returns] = None,
                      turnOverEstimates: Option[TurnoverEstimates] = None,
                      bankAccount : Option[BankAccount] = None,
                      vatFlatRateScheme: Option[VatFlatRateScheme] = None,
                      status: VatRegStatus.Value
                    )

object VatScheme {
  implicit val frmt = TradingDetails.apiFormat
  implicit val format: OFormat[VatScheme] = (
    (__ \ "registrationId").format[String] and
      (__ \ "lodgingOfficer").formatNullable[LodgingOfficer].inmap[Option[LodgingOfficer]](_ => Option.empty[LodgingOfficer], _ => Option.empty[LodgingOfficer]) and
      (__ \ "tradingDetails").formatNullable[TradingDetails] and
      (__ \ "financials").formatNullable[VatFinancials] and
      (__ \ "vatSicAndCompliance").formatNullable[VatSicAndCompliance] and
      (__ \ "vatContact").formatNullable[VatContact] and
      (__ \ "threshold").formatNullable[Threshold] and
      (__ \ "returns").formatNullable[Returns] and
      (__ \ "turnoverEstimates").formatNullable[TurnoverEstimates] and
      (__ \ "bankAccount").formatNullable[BankAccount] and
      (__ \ "vatFlatRateScheme").formatNullable[VatFlatRateScheme] and
      (__ \ "status").format[VatRegStatus.Value]
    ) (VatScheme.apply, unlift(VatScheme.unapply))

}
