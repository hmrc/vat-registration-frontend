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

package models.api.vatapplication

import models.{ApiKey, NIPTurnover}
import play.api.libs.json._

import java.time.LocalDate

case class VatApplication(tradeVatGoodsOutsideUk: Option[Boolean] = None,
                          eoriRequested: Option[Boolean] = None,
                          standardRateSupplies: Option[BigDecimal] = None,
                          reducedRateSupplies: Option[BigDecimal] = None,
                          zeroRatedSupplies: Option[BigDecimal] = None,
                          turnoverEstimate: Option[BigDecimal] = None,
                          acceptTurnOverEstimate: Option[Boolean] = None,
                          northernIrelandProtocol: Option[NIPTurnover] = None,
                          claimVatRefunds: Option[Boolean] = None,
                          appliedForExemption: Option[Boolean] = None,
                          overseasCompliance: Option[OverseasCompliance] = None,
                          startDate: Option[LocalDate] = None,
                          returnsFrequency: Option[ReturnsFrequency] = None,
                          staggerStart: Option[Stagger] = None,
                          annualAccountingDetails: Option[AASDetails] = None,
                          hasTaxRepresentative: Option[Boolean] = None,
                          currentlyTrading: Option[Boolean] = None)

object VatApplication {
  implicit val apiKey: ApiKey[VatApplication] = ApiKey("vatApplication")
  implicit val format: Format[VatApplication] = Json.format[VatApplication]
}