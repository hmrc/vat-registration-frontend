/*
 * Copyright 2017 HM Revenue & Customs
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

package models

import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration, VoluntaryRegistrationReason}


case class S4LVatFinancials
(
  estimateVatTurnover: Option[EstimateVatTurnover],
  zeroRatedTurnoverEstimate: Option[EstimateZeroRatedSales],
  vatChargeExpectancy: Option[VatChargeExpectancy],
  vatReturnFrequency: Option[VatReturnFrequency],
  accountingPeriod: Option[AccountingPeriod],
  companyBankAccountDetails: Option[CompanyBankAccountDetails]
)

case class S4LTradingDetails
(
  tradingName: Option[TradingNameView],
  startDate: Option[StartDateView],
  voluntaryRegistration: Option[VoluntaryRegistration],
  voluntaryRegistrationReason: Option[VoluntaryRegistrationReason]
)

case class S4LVatSicAndCompliance
(
  description: Option[BusinessActivityDescription],
  culturalCompliance: Option[NotForProfit],
  labourComplianceCompanyProvideWorkers: Option[CompanyProvideWorkers],
  labourComplianceWorkers: Option[Workers],
  labourComplianceTemporaryContracts: Option[TemporaryContracts],
  labourComplianceSkilledWorkers: Option[SkilledWorkers]

)
