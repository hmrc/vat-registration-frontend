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

import models.api.{VatScheme, VatServiceEligibility}
import models.view.frs._
import models.view.ppob.PpobView
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import play.api.libs.json.{Json, OFormat}

trait S4LModelTransformer[G] {
  // Returns a view model for a specific part of a given VatScheme API model
  def toS4LModel(vatScheme: VatScheme): G
}

final case class S4LVatFinancials
(
  estimateVatTurnover: Option[EstimateVatTurnover] = None,
  zeroRatedTurnover: Option[ZeroRatedSales] = None,
  zeroRatedTurnoverEstimate: Option[EstimateZeroRatedSales] = None,
  vatChargeExpectancy: Option[VatChargeExpectancy] = None,
  vatReturnFrequency: Option[VatReturnFrequency] = None,
  accountingPeriod: Option[AccountingPeriod] = None,
  companyBankAccount: Option[CompanyBankAccount] = None,
  companyBankAccountDetails: Option[CompanyBankAccountDetails] = None
)

object S4LVatFinancials {
  implicit val format: OFormat[S4LVatFinancials] = Json.format[S4LVatFinancials]

  implicit val transformer = new S4LModelTransformer[S4LVatFinancials] {
    @Override def toS4LModel(vs: VatScheme): S4LVatFinancials =
      S4LVatFinancials(
        estimateVatTurnover = ApiModelTransformer[EstimateVatTurnover].toViewModel(vs),
        zeroRatedTurnover = ApiModelTransformer[ZeroRatedSales].toViewModel(vs),
        zeroRatedTurnoverEstimate = ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vs),
        vatChargeExpectancy = ApiModelTransformer[VatChargeExpectancy].toViewModel(vs),
        vatReturnFrequency = ApiModelTransformer[VatReturnFrequency].toViewModel(vs),
        accountingPeriod = ApiModelTransformer[AccountingPeriod].toViewModel(vs),
        companyBankAccount = ApiModelTransformer[CompanyBankAccount].toViewModel(vs),
        companyBankAccountDetails = ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs)
      )
  }
}

final case class S4LTradingDetails
(
  taxableTurnover: Option[TaxableTurnover] = None,
  tradingName: Option[TradingNameView] = None,
  startDate: Option[StartDateView] = None,
  voluntaryRegistration: Option[VoluntaryRegistration] = None,
  voluntaryRegistrationReason: Option[VoluntaryRegistrationReason] = None,
  euGoods: Option[EuGoods] = None,
  applyEori: Option[ApplyEori] = None
)

object S4LTradingDetails {
  implicit val format: OFormat[S4LTradingDetails] = Json.format[S4LTradingDetails]

  implicit val transformer = new S4LModelTransformer[S4LTradingDetails] {
    @Override def toS4LModel(vs: VatScheme): S4LTradingDetails =
      S4LTradingDetails(
        taxableTurnover = ApiModelTransformer[TaxableTurnover].toViewModel(vs),
        tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
        startDate = ApiModelTransformer[StartDateView].toViewModel(vs),
        voluntaryRegistration = ApiModelTransformer[VoluntaryRegistration].toViewModel(vs),
        voluntaryRegistrationReason = ApiModelTransformer[VoluntaryRegistrationReason].toViewModel(vs),
        euGoods = ApiModelTransformer[EuGoods].toViewModel(vs),
        applyEori = ApiModelTransformer[ApplyEori].toViewModel(vs)
      )
  }
}

final case class S4LVatSicAndCompliance
(
  description: Option[BusinessActivityDescription] = None,
  mainBusinessActivity: Option[MainBusinessActivityView] = None,

  //Cultural Compliance
  notForProfit: Option[NotForProfit] = None,

  //Labour Compliance
  companyProvideWorkers: Option[CompanyProvideWorkers] = None,
  workers: Option[Workers] = None,
  temporaryContracts: Option[TemporaryContracts] = None,
  skilledWorkers: Option[SkilledWorkers] = None,

  //Financial Compliance
  adviceOrConsultancy: Option[AdviceOrConsultancy] = None,
  actAsIntermediary: Option[ActAsIntermediary] = None,
  chargeFees: Option[ChargeFees] = None,
  leaseVehicles: Option[LeaseVehicles] = None,
  additionalNonSecuritiesWork: Option[AdditionalNonSecuritiesWork] = None,
  discretionaryInvestmentManagementServices: Option[DiscretionaryInvestmentManagementServices] = None,
  investmentFundManagement: Option[InvestmentFundManagement] = None,
  manageAdditionalFunds: Option[ManageAdditionalFunds] = None
)

object S4LVatSicAndCompliance {
  implicit val format: OFormat[S4LVatSicAndCompliance] = Json.format[S4LVatSicAndCompliance]

  implicit val transformer = new S4LModelTransformer[S4LVatSicAndCompliance] {
    @Override def toS4LModel(vs: VatScheme): S4LVatSicAndCompliance =
      S4LVatSicAndCompliance(
        description = ApiModelTransformer[BusinessActivityDescription].toViewModel(vs),
        mainBusinessActivity = ApiModelTransformer[MainBusinessActivityView].toViewModel(vs),

        notForProfit = ApiModelTransformer[NotForProfit].toViewModel(vs),

        companyProvideWorkers = ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs),
        workers = ApiModelTransformer[Workers].toViewModel(vs),
        temporaryContracts = ApiModelTransformer[TemporaryContracts].toViewModel(vs),
        skilledWorkers = ApiModelTransformer[SkilledWorkers].toViewModel(vs),

        adviceOrConsultancy = ApiModelTransformer[AdviceOrConsultancy].toViewModel(vs),
        actAsIntermediary = ApiModelTransformer[ActAsIntermediary].toViewModel(vs),
        chargeFees = ApiModelTransformer[ChargeFees].toViewModel(vs),
        leaseVehicles = ApiModelTransformer[LeaseVehicles].toViewModel(vs),
        additionalNonSecuritiesWork = ApiModelTransformer[AdditionalNonSecuritiesWork].toViewModel(vs),
        discretionaryInvestmentManagementServices = ApiModelTransformer[DiscretionaryInvestmentManagementServices].toViewModel(vs),
        investmentFundManagement = ApiModelTransformer[InvestmentFundManagement].toViewModel(vs),
        manageAdditionalFunds = ApiModelTransformer[ManageAdditionalFunds].toViewModel(vs)
      )
  }
}

final case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails] = None
)

object S4LVatContact {
  implicit val format: OFormat[S4LVatContact] = Json.format[S4LVatContact]

  implicit val transformer = new S4LModelTransformer[S4LVatContact] {
    @Override def toS4LModel(vs: VatScheme): S4LVatContact =
      S4LVatContact(businessContactDetails = ApiModelTransformer[BusinessContactDetails].toViewModel(vs))
  }
}

final case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility] = None
)

object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]

  implicit val transformer = new S4LModelTransformer[S4LVatEligibility] {
    @Override def toS4LModel(vs: VatScheme): S4LVatEligibility =
      S4LVatEligibility(vatEligibility = ApiModelTransformer[VatServiceEligibility].toViewModel(vs))
  }
}

final case class S4LVatLodgingOfficer
(
  officerHomeAddress: Option[OfficerHomeAddressView] = None,
  officerDateOfBirth: Option[OfficerDateOfBirthView] = None,
  officerNino: Option[OfficerNinoView] = None,
  completionCapacity: Option[CompletionCapacityView] = None,
  officerContactDetails: Option[OfficerContactDetailsView] = None,
  formerName: Option[FormerNameView] = None,
  formerNameDate: Option[FormerNameDateView] = None,
  previousAddress: Option[PreviousAddressView] = None
)

object S4LVatLodgingOfficer {
  implicit val format: OFormat[S4LVatLodgingOfficer] = Json.format[S4LVatLodgingOfficer]

  implicit val transformer = new S4LModelTransformer[S4LVatLodgingOfficer] {
    @Override def toS4LModel(vs: VatScheme): S4LVatLodgingOfficer =
      S4LVatLodgingOfficer(
        officerHomeAddress = ApiModelTransformer[OfficerHomeAddressView].toViewModel(vs),
        officerDateOfBirth = ApiModelTransformer[OfficerDateOfBirthView].toViewModel(vs),
        officerNino = ApiModelTransformer[OfficerNinoView].toViewModel(vs),
        completionCapacity = ApiModelTransformer[CompletionCapacityView].toViewModel(vs),
        officerContactDetails = ApiModelTransformer[OfficerContactDetailsView].toViewModel(vs),
        formerName = ApiModelTransformer[FormerNameView].toViewModel(vs),
        formerNameDate = ApiModelTransformer[FormerNameDateView].toViewModel(vs),
        previousAddress = ApiModelTransformer[PreviousAddressView].toViewModel(vs)
      )
  }
}

final case class S4LPpob
(
  address: Option[PpobView] = None
)

object S4LPpob {
  implicit val format: OFormat[S4LPpob] = Json.format[S4LPpob]

  implicit val transformer = new S4LModelTransformer[S4LPpob] {
    @Override def toS4LModel(vs: VatScheme): S4LPpob =
      S4LPpob(address = ApiModelTransformer[PpobView].toViewModel(vs))
  }
}

final case class S4LFlatRateScheme
(
  joinFrs: Option[JoinFrsView] = None,
  annualCostsInclusive: Option[AnnualCostsInclusiveView] = None,
  annualCostsLimited: Option[AnnualCostsLimitedView] = None,
  registerForFrs: Option[RegisterForFrsView] = None,
  frsStartDate: Option[FrsStartDateView] = None,
  categoryOfBusiness: Option[BusinessSectorView] = None
)

object S4LFlatRateScheme {
  implicit val format: OFormat[S4LFlatRateScheme] = Json.format[S4LFlatRateScheme]

  implicit val transformer = new S4LModelTransformer[S4LFlatRateScheme] {
    @Override def toS4LModel(vs: VatScheme): S4LFlatRateScheme =
      S4LFlatRateScheme(
        joinFrs = ApiModelTransformer[JoinFrsView].toViewModel(vs),
        annualCostsInclusive = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
        annualCostsLimited = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
        registerForFrs = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
        frsStartDate = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
        categoryOfBusiness = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
      )
  }
}
