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

import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api.{VatFinancials, _}
import models.view.frs._
import models.view.ppob.PpobView
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.cultural.NotForProfit.NOT_PROFIT_YES
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.CompanyProvideWorkers.PROVIDE_WORKERS_YES
import models.view.sicAndCompliance.labour.SkilledWorkers.SKILLED_WORKERS_YES
import models.view.sicAndCompliance.labour.TemporaryContracts.TEMP_CONTRACTS_YES
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_YES
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.MONTHLY
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
import models.view.vatTradingDetails.vatChoice.StartDateView.BUSINESS_START_DATE
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_YES
import models.view.vatTradingDetails.vatChoice._
import models.view.vatTradingDetails.vatEuTrading.EuGoods.EU_GOODS_YES
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import play.api.libs.json.{Json, OFormat}

trait S4LModelTransformer[C] {
  // Returns an S4L container for a logical group given a VatScheme
  def toS4LModel(vatScheme: VatScheme): C
}

trait S4LApiTransformer[C, API] {
  // Returns logical group API model given an S4L container
  def toApi(container: C, group: API): API
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

  implicit val modelT = new S4LModelTransformer[S4LVatFinancials] {
    // map VatScheme to S4LVatFinancials
    override def toS4LModel(vs: VatScheme): S4LVatFinancials =
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

  implicit val apiT = new S4LApiTransformer[S4LVatFinancials, VatFinancials] {
    // map S4LVatFinancials to VatFinancials
    override def toApi(c: S4LVatFinancials, g: VatFinancials): VatFinancials =
      g.copy(
        bankAccount = c.companyBankAccountDetails.map(cad =>
          VatBankAccount(
            accountName = cad.accountName,
            accountSortCode = cad.sortCode,
            accountNumber = cad.accountNumber)),
        turnoverEstimate = c.estimateVatTurnover.map(_.vatTurnoverEstimate).getOrElse(g.turnoverEstimate),
        zeroRatedTurnoverEstimate = c.zeroRatedTurnoverEstimate.map(_.zeroRatedTurnoverEstimate),
        reclaimVatOnMostReturns = c.vatChargeExpectancy.map
                  (_.yesNo == VAT_CHARGE_YES).getOrElse(g.reclaimVatOnMostReturns),
        accountingPeriods = g.accountingPeriods.copy(
                              frequency = c.vatReturnFrequency.map(_.frequencyType).getOrElse(MONTHLY),
                              periodStart = c.vatReturnFrequency.flatMap(_ => c.accountingPeriod.map(_.accountingPeriod.toLowerCase())))
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
  applyEori: Option[ApplyEori] = None,
  overThreshold: Option[OverThresholdView] = None
)

object S4LTradingDetails {
  implicit val format: OFormat[S4LTradingDetails] = Json.format[S4LTradingDetails]

  implicit val modelT = new S4LModelTransformer[S4LTradingDetails] {
    // map VatScheme to VatTradingDetails
    override def toS4LModel(vs: VatScheme): S4LTradingDetails =
      S4LTradingDetails(
        taxableTurnover = ApiModelTransformer[TaxableTurnover].toViewModel(vs),
        tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
        startDate = ApiModelTransformer[StartDateView].toViewModel(vs),
        voluntaryRegistration = ApiModelTransformer[VoluntaryRegistration].toViewModel(vs),
        voluntaryRegistrationReason = ApiModelTransformer[VoluntaryRegistrationReason].toViewModel(vs),
        euGoods = ApiModelTransformer[EuGoods].toViewModel(vs),
        applyEori = ApiModelTransformer[ApplyEori].toViewModel(vs),
        overThreshold = ApiModelTransformer[OverThresholdView].toViewModel(vs)
      )
  }
  implicit val apiT = new S4LApiTransformer[S4LTradingDetails, VatTradingDetails] {
    // map S4LTradingDetails to VatTradingDetails
    override def toApi(c: S4LTradingDetails, g: VatTradingDetails): VatTradingDetails =
      g.copy(
        vatChoice = g.vatChoice.copy(
          necessity = c.voluntaryRegistration.map(vr =>
            if (vr.yesNo == REGISTER_YES) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY).getOrElse(g.vatChoice.necessity),
          vatStartDate = c.startDate.map(sd => g.vatChoice.vatStartDate.copy(
              selection = sd.dateType,
              startDate = if (sd.dateType == BUSINESS_START_DATE) sd.ctActiveDate else sd.date
              )
            ).getOrElse(g.vatChoice.vatStartDate),
          reason = c.voluntaryRegistrationReason.map(_.reason)),

        tradingName = c.tradingName.map(tnv =>
          TradingName(tnv.yesNo == TRADING_NAME_YES, tnv.tradingName)).getOrElse(g.tradingName),

        euTrading = g.euTrading.copy(
          selection = c.euGoods.map(_.yesNo == EU_GOODS_YES).getOrElse(g.euTrading.selection),
          eoriApplication = c.applyEori.map(_.yesNo)
        )
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
  // utilities
  def dropAllCompliance(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    deleteCultural(deleteLabour(deleteFinance(container)))

  def financeOnly(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    deleteCultural(deleteLabour(container))

  def labourOnly(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    deleteCultural(deleteFinance(container))

  def culturalOnly(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    deleteLabour(deleteFinance(container))

  // labour List(LabProvidesWorkersPath, LabWorkersPath, LabTempContractsPath, LabSkilledWorkersPath)
  def dropFromCompanyProvideWorkers(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(workers = None, temporaryContracts = None, skilledWorkers = None)

  def dropFromWorkers(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(temporaryContracts = None, skilledWorkers = None)

  def dropFromTemporaryContracts(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(skilledWorkers = None)


  // finance
  def dropFromActAsIntermediary(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(chargeFees = None,
      additionalNonSecuritiesWork = None,
      discretionaryInvestmentManagementServices = None,
      leaseVehicles = None,
      investmentFundManagement = None,
      manageAdditionalFunds = None)

  def dropFromAddNonSecurities(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(
      discretionaryInvestmentManagementServices = None,
      leaseVehicles = None,
      investmentFundManagement = None,
      manageAdditionalFunds = None)

  def dropFromDiscInvManServices(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(leaseVehicles = None, investmentFundManagement = None, manageAdditionalFunds = None)

  def dropFromLeaseVehicles(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(investmentFundManagement = None, manageAdditionalFunds = None)

  def dropFromInvFundManagement(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(manageAdditionalFunds = None)



  private def deleteFinance(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(
      adviceOrConsultancy = None,
      actAsIntermediary = None,
      chargeFees = None,
      leaseVehicles = None,
      additionalNonSecuritiesWork = None,
      discretionaryInvestmentManagementServices = None,
      investmentFundManagement = None,
      manageAdditionalFunds = None
    )

  private def deleteLabour(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(
      companyProvideWorkers = None,
      workers = None,
      temporaryContracts = None,
      skilledWorkers = None
    )

  private def deleteCultural(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(notForProfit = None)

  implicit val format: OFormat[S4LVatSicAndCompliance] = Json.format[S4LVatSicAndCompliance]

  implicit val modelT = new S4LModelTransformer[S4LVatSicAndCompliance] {
    override def toS4LModel(vs: VatScheme): S4LVatSicAndCompliance =
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

  implicit val apiT = new S4LApiTransformer[S4LVatSicAndCompliance, VatSicAndCompliance] {
    override def toApi(c: S4LVatSicAndCompliance, g: VatSicAndCompliance): VatSicAndCompliance =
      g.copy(
        businessDescription = c.description.map(_.description).getOrElse(g.businessDescription),
        mainBusinessActivity = c.mainBusinessActivity.flatMap(_.mainBusinessActivity).getOrElse(g.mainBusinessActivity),

        culturalCompliance = c.notForProfit.map(nfp => VatComplianceCultural(nfp.yesNo == NOT_PROFIT_YES)),

        labourCompliance = c.companyProvideWorkers.map(cpw =>
                                VatComplianceLabour(
                                  labour = cpw.yesNo == PROVIDE_WORKERS_YES,
                                  workers = c.workers.map(_.numberOfWorkers),
                                  temporaryContracts = c.temporaryContracts.map(_.yesNo == TEMP_CONTRACTS_YES),
                                  skilledWorkers = c.skilledWorkers.map(_.yesNo == SKILLED_WORKERS_YES))),

        financialCompliance = c.adviceOrConsultancy.map(ac =>
                                VatComplianceFinancial(
                                  adviceOrConsultancyOnly = ac.yesNo,
                                  actAsIntermediary = c.actAsIntermediary.exists(_.yesNo),
                                  chargeFees = c.chargeFees.map(_.yesNo),
                                  additionalNonSecuritiesWork = c.additionalNonSecuritiesWork.map(_.yesNo),
                                  discretionaryInvestmentManagementServices = c.discretionaryInvestmentManagementServices.map(_.yesNo),
                                  vehicleOrEquipmentLeasing = c.leaseVehicles.map(_.yesNo),
                                  investmentFundManagementServices = c.investmentFundManagement.map(_.yesNo),
                                  manageFundsAdditional = c.manageAdditionalFunds.map(_.yesNo)
                                ))

      )
  }
}

final case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails] = None
)

object S4LVatContact {
  implicit val format: OFormat[S4LVatContact] = Json.format[S4LVatContact]

  implicit val modelT = new S4LModelTransformer[S4LVatContact] {
    override def toS4LModel(vs: VatScheme): S4LVatContact =
      S4LVatContact(businessContactDetails = ApiModelTransformer[BusinessContactDetails].toViewModel(vs))
  }

  implicit val apiT = new S4LApiTransformer[S4LVatContact, VatContact] {
    override def toApi(c: S4LVatContact, g: VatContact): VatContact =
      c.businessContactDetails.map( bc => VatContact(
        VatDigitalContact(email = bc.email, tel = bc.daytimePhone, mobile = bc.mobile), website = bc.website)).
        getOrElse(g)

  }
}

final case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility] = None
)

object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]

  implicit val modelT = new S4LModelTransformer[S4LVatEligibility] {
    override def toS4LModel(vs: VatScheme): S4LVatEligibility =
      S4LVatEligibility(vatEligibility = ApiModelTransformer[VatServiceEligibility].toViewModel(vs))
  }

  implicit val apiT = new S4LApiTransformer[S4LVatEligibility, VatServiceEligibility] {
    override def toApi(c: S4LVatEligibility, g: VatServiceEligibility): VatServiceEligibility = {
      c.vatEligibility.map(ve => VatServiceEligibility(
        haveNino = ve.haveNino,
        doingBusinessAbroad = ve.doingBusinessAbroad,
        doAnyApplyToYou = ve.doAnyApplyToYou,
        applyingForAnyOf = ve.applyingForAnyOf,
        companyWillDoAnyOf = ve.companyWillDoAnyOf)
      ).getOrElse(g)
    }
  }
}


final case class S4LVatLodgingOfficer
(
  officerHomeAddress: Option[OfficerHomeAddressView] = None,
  officerSecurityQuestions: Option[OfficerSecurityQuestionsView] = None,
  completionCapacity: Option[CompletionCapacityView] = None,
  officerContactDetails: Option[OfficerContactDetailsView] = None,
  formerName: Option[FormerNameView] = None,
  formerNameDate: Option[FormerNameDateView] = None,
  previousAddress: Option[PreviousAddressView] = None
)

object S4LVatLodgingOfficer {
  implicit val format: OFormat[S4LVatLodgingOfficer] = Json.format[S4LVatLodgingOfficer]


  implicit val modelT = new S4LModelTransformer[S4LVatLodgingOfficer] {
    override def toS4LModel(vs: VatScheme): S4LVatLodgingOfficer =
      S4LVatLodgingOfficer(
        officerHomeAddress = ApiModelTransformer[OfficerHomeAddressView].toViewModel(vs),
        officerSecurityQuestions = ApiModelTransformer[OfficerSecurityQuestionsView].toViewModel(vs),
        completionCapacity = ApiModelTransformer[CompletionCapacityView].toViewModel(vs),
        officerContactDetails = ApiModelTransformer[OfficerContactDetailsView].toViewModel(vs),
        formerName = ApiModelTransformer[FormerNameView].toViewModel(vs),
        formerNameDate = ApiModelTransformer[FormerNameDateView].toViewModel(vs),
        previousAddress = ApiModelTransformer[PreviousAddressView].toViewModel(vs)
      )
  }

  implicit val apiT = new S4LApiTransformer[S4LVatLodgingOfficer, VatLodgingOfficer] {
    override def toApi(c: S4LVatLodgingOfficer, g: VatLodgingOfficer): VatLodgingOfficer =
      g.copy(
        currentAddress = c.officerHomeAddress.flatMap(_.address).getOrElse(g.currentAddress),
        dob = c.officerSecurityQuestions.map(d => DateOfBirth(d.dob)).getOrElse(g.dob),
        nino = c.officerSecurityQuestions.map(n => n.nino).getOrElse(g.nino),
        role = c.completionCapacity.flatMap(_.completionCapacity.map(_.role)).getOrElse(g.role),
        name = c.completionCapacity.flatMap(_.completionCapacity.map(_.name)).getOrElse(g.name),

        changeOfName = c.formerName.map((fnv: FormerNameView) =>
          ChangeOfName(nameHasChanged = fnv.yesNo,
                        formerName = fnv.formerName.map(fn =>
                          FormerName(formerName = fn,
                                      dateOfNameChange = c.formerNameDate.map(_.date))))).getOrElse(g.changeOfName),

        currentOrPreviousAddress = c.previousAddress.map(cpav =>
          CurrentOrPreviousAddress(currentAddressThreeYears = cpav.yesNo,
                                    previousAddress = cpav.address)).getOrElse(g.currentOrPreviousAddress),

        contact = c.officerContactDetails.map(ocd =>
           OfficerContactDetails(email = ocd.email, tel = ocd.daytimePhone, mobile = ocd.mobile)).getOrElse(g.contact)
      )
  }
}


final case class S4LPpob
(
  address: Option[PpobView] = None
)

object S4LPpob {
  implicit val format: OFormat[S4LPpob] = Json.format[S4LPpob]

  implicit val modelT = new S4LModelTransformer[S4LPpob] {
    override def toS4LModel(vs: VatScheme): S4LPpob =
      S4LPpob(address = ApiModelTransformer[PpobView].toViewModel(vs))
  }

  implicit val transformer = new S4LApiTransformer[S4LPpob, ScrsAddress] {
    // TODO PPOB sits directly under VatScheme root !! see VRS.submitPpob()
    override def toApi(c: S4LPpob, g: ScrsAddress): ScrsAddress = g
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

  implicit val modelT = new S4LModelTransformer[S4LFlatRateScheme] {
    override def toS4LModel(vs: VatScheme): S4LFlatRateScheme =
      S4LFlatRateScheme(
        joinFrs = ApiModelTransformer[JoinFrsView].toViewModel(vs),
        annualCostsInclusive = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
        annualCostsLimited = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
        registerForFrs = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
        frsStartDate = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
        categoryOfBusiness = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
      )
  }

  implicit val apiT = new S4LApiTransformer[S4LFlatRateScheme, VatFlatRateScheme] {
    override def toApi(c: S4LFlatRateScheme, g: VatFlatRateScheme): VatFlatRateScheme =
      g.copy(
        joinFrs = c.joinFrs.map(_.selection).getOrElse(false),
        annualCostsInclusive = c.annualCostsInclusive.map(_.selection),
        annualCostsLimited = c.annualCostsLimited.map(_.selection),
        doYouWantToUseThisRate = c.registerForFrs.map(_.selection),
        whenDoYouWantToJoinFrs = c.frsStartDate.map(_.dateType),
        startDate = c.frsStartDate.flatMap(_.date),
        categoryOfBusiness = c.categoryOfBusiness.map(_.businessSector),
        percentage = c.categoryOfBusiness.map(_.flatRatePercentage))
  }
}

