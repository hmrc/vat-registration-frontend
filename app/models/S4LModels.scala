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

import models.api.VatServiceEligibility
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}


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
  voluntaryRegistrationReason: Option[VoluntaryRegistrationReason],
  euGoods: Option[EuGoods],
  applyEori: Option[ApplyEori]
)

case class S4LVatSicAndCompliance
(
  description: Option[BusinessActivityDescription],

  //Cultural Compliance
  notForProfit: Option[NotForProfit],

  //Labour Compliance
  companyProvideWorkers: Option[CompanyProvideWorkers],
  workers: Option[Workers],
  temporaryContracts: Option[TemporaryContracts],
  skilledWorkers: Option[SkilledWorkers],

  //Financial Compliance
  adviceOrConsultancy: Option[AdviceOrConsultancy],
  actAsIntermediary: Option[ActAsIntermediary],
  chargeFees: Option[ChargeFees],
  leaseVehicles: Option[LeaseVehicles],
  additionalNonSecuritiesWork: Option[AdditionalNonSecuritiesWork],
  discretionaryInvestmentManagementServices: Option[DiscretionaryInvestmentManagementServices],
  investmentFundManagement: Option[InvestmentFundManagement],
  manageAdditionalFunds: Option[ManageAdditionalFunds]
)

case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails]
)

case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility]
)

case class S4LVatLodgingOfficer
(
  officerHomeAddressView: Option[OfficerHomeAddressView]
)

