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
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerDateOfBirthView, OfficerHomeAddressView, OfficerNinoView}
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import play.api.libs.json.{Json, OFormat}


final case class S4LVatFinancials
(
  estimateVatTurnover: Option[EstimateVatTurnover],
  zeroRatedTurnoverEstimate: Option[EstimateZeroRatedSales],
  vatChargeExpectancy: Option[VatChargeExpectancy],
  vatReturnFrequency: Option[VatReturnFrequency],
  accountingPeriod: Option[AccountingPeriod],
  companyBankAccountDetails: Option[CompanyBankAccountDetails]
)

object S4LVatFinancials {
  implicit val format: OFormat[S4LVatFinancials] = Json.format[S4LVatFinancials]
}

final case class S4LTradingDetails
(
  tradingName: Option[TradingNameView],
  startDate: Option[StartDateView],
  voluntaryRegistration: Option[VoluntaryRegistration],
  voluntaryRegistrationReason: Option[VoluntaryRegistrationReason],
  euGoods: Option[EuGoods],
  applyEori: Option[ApplyEori]
)

object S4LTradingDetails {
  implicit val format: OFormat[S4LTradingDetails] = Json.format[S4LTradingDetails]
}

final case class S4LVatSicAndCompliance
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

object S4LVatSicAndCompliance {
  implicit val format: OFormat[S4LVatSicAndCompliance] = Json.format[S4LVatSicAndCompliance]
}

final case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails]
)

object S4LVatContact {
  implicit val format: OFormat[S4LVatContact] = Json.format[S4LVatContact]
}

final case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility]
)

object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]
}

final case class S4LVatLodgingOfficer
(
  officerHomeAddressView: Option[OfficerHomeAddressView],
  officerDateOfBirthView: Option[OfficerDateOfBirthView],
  officerNinoView: Option[OfficerNinoView],
  completionCapacityView: Option[CompletionCapacityView]
)

object S4LVatLodgingOfficer {
  implicit val format: OFormat[S4LVatLodgingOfficer] = Json.format[S4LVatLodgingOfficer]
}

