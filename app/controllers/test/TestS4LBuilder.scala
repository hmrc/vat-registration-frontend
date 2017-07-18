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

package controllers.test

import java.time.LocalDate

import models._
import models.api._
import models.view.frs._
import models.view.ppob.PpobView
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.test.TestSetup
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.TradingNameView._
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}

class TestS4LBuilder {

  def tradingDetailsFromData(data: TestSetup): S4LTradingDetails = {
    val taxableTurnover: Option[String] = data.vatChoice.taxableTurnoverChoice

    val startDate = data.vatChoice.startDateChoice match {
      case None => StartDateView()
      case Some("SPECIFIC_DATE") => StartDateView(dateType = "SPECIFIC_DATE", date = Some(LocalDate.of(
        data.vatChoice.startDateYear.map(_.toInt).get,
        data.vatChoice.startDateMonth.map(_.toInt).get,
        data.vatChoice.startDateDay.map(_.toInt).get
      )))
      case Some("BUSINESS_START_DATE") => StartDateView(dateType = "BUSINESS_START_DATE", ctActiveDate = Some(LocalDate.of(
        data.vatChoice.startDateYear.map(_.toInt).get,
        data.vatChoice.startDateMonth.map(_.toInt).get,
        data.vatChoice.startDateDay.map(_.toInt).get
      )))
      case Some(t) => StartDateView(t, None)
    }

    val voluntaryRegistration: Option[String] = data.vatChoice.voluntaryChoice
    val voluntaryRegistrationReason: Option[String] = data.vatChoice.voluntaryRegistrationReason

    val tradingName = data.vatTradingDetails.tradingNameChoice.map(_ =>
      TradingName(
        selection = data.vatTradingDetails.tradingNameChoice.fold(false)(_ == TRADING_NAME_YES),
        tradingName = data.vatTradingDetails.tradingName))

    val euGoods: Option[String] = data.vatTradingDetails.euGoods
    val applyEori: Option[String] = data.vatTradingDetails.applyEori

    S4LTradingDetails(
      taxableTurnover = taxableTurnover.map(TaxableTurnover(_)),
      startDate = Some(startDate),
      voluntaryRegistration = voluntaryRegistration.map(VoluntaryRegistration(_)),
      voluntaryRegistrationReason = voluntaryRegistrationReason.map(VoluntaryRegistrationReason(_)),
      tradingName = tradingName.map(t => TradingNameView(if (t.selection) TRADING_NAME_YES else TRADING_NAME_NO, t.tradingName)),
      euGoods = euGoods.map(EuGoods(_)),
      applyEori = applyEori.map(a => ApplyEori(a.toBoolean))
    )
  }

  def vatFinancialsFromData(data: TestSetup): S4LVatFinancials = {
    val fin = data.vatFinancials

    val estimateVatTurnover = fin.estimateVatTurnover.map(x => EstimateVatTurnover(x.toLong))
    val zeroRatedTurnover = fin.zeroRatedSalesChoice.map(ZeroRatedSales.apply)
    val zeroRatedTurnoverEstimate = fin.zeroRatedTurnoverEstimate.map(x => EstimateZeroRatedSales(x.toLong))
    val vatChargeExpectancy = fin.vatChargeExpectancyChoice.map(VatChargeExpectancy.apply)
    val vatReturnFrequency = fin.vatReturnFrequency.map(VatReturnFrequency.apply)
    val accountingPeriod = fin.accountingPeriod.map(AccountingPeriod.apply)
    val companyBankAccount = fin.companyBankAccountChoice.map(CompanyBankAccount.apply)
    val companyBankAccountDetails = fin.companyBankAccountName.map(name =>
      CompanyBankAccountDetails(name, fin.companyBankAccountNumber.get, fin.sortCode.get)
    )
    S4LVatFinancials(
      estimateVatTurnover = estimateVatTurnover,
      zeroRatedTurnover = zeroRatedTurnover,
      zeroRatedTurnoverEstimate = zeroRatedTurnoverEstimate,
      vatChargeExpectancy = vatChargeExpectancy,
      vatReturnFrequency = vatReturnFrequency,
      accountingPeriod = accountingPeriod,
      companyBankAccount = companyBankAccount,
      companyBankAccountDetails = companyBankAccountDetails
    )
  }

  def vatSicAndComplianceFromData(data: TestSetup): S4LVatSicAndCompliance = {
    val base = data.sicAndCompliance
    val compliance: S4LVatSicAndCompliance =
      (base.culturalNotForProfit, base.labourCompanyProvideWorkers, base.financialAdviceOrConsultancy) match {
        case (Some(_), None, None) => S4LVatSicAndCompliance(
          notForProfit = Some(NotForProfit(base.culturalNotForProfit.get)))
        case (None, Some(_), None) => S4LVatSicAndCompliance(
          companyProvideWorkers = base.labourCompanyProvideWorkers.flatMap(x => Some(CompanyProvideWorkers(x))),
          workers = base.labourWorkers.flatMap(x => Some(Workers(x.toInt))),
          temporaryContracts = base.labourTemporaryContracts.flatMap(x => Some(TemporaryContracts(x))),
          skilledWorkers = base.labourSkilledWorkers.flatMap(x => Some(SkilledWorkers(x))))
        case (None, None, Some(_)) => S4LVatSicAndCompliance(
          adviceOrConsultancy = base.financialAdviceOrConsultancy.flatMap(x => Some(AdviceOrConsultancy(x.toBoolean))),
          actAsIntermediary = base.financialActAsIntermediary.flatMap(x => Some(ActAsIntermediary(x.toBoolean))),
          chargeFees = base.financialChargeFees.flatMap(x => Some(ChargeFees(x.toBoolean))),
          leaseVehicles = base.financialLeaseVehiclesOrEquipment.flatMap(x => Some(LeaseVehicles(x.toBoolean))),
          additionalNonSecuritiesWork = base.financialAdditionalNonSecuritiesWork.flatMap(x => Some(AdditionalNonSecuritiesWork(x.toBoolean))),
          discretionaryInvestmentManagementServices =
            base.financialDiscretionaryInvestment.flatMap(x => Some(DiscretionaryInvestmentManagementServices(x.toBoolean))),
          investmentFundManagement = base.financialInvestmentFundManagement.flatMap(x => Some(InvestmentFundManagement(x.toBoolean))),
          manageAdditionalFunds = base.financialManageAdditionalFunds.flatMap(x => Some(ManageAdditionalFunds(x.toBoolean))))
        case (_, _, _) => S4LVatSicAndCompliance()
      }

    compliance.copy(
      description = base.businessActivityDescription.map(BusinessActivityDescription(_)),
      mainBusinessActivity = Some(MainBusinessActivityView(SicCode(
        id = base.mainBusinessActivityId.getOrElse(""),
        description = base.mainBusinessActivityDescription.getOrElse(""),
        displayDetails = base.mainBusinessActivityDisplayDetails.getOrElse("")
      )))
    )
  }

  def vatContactFromData(data: TestSetup): S4LVatContact = {
    val businessContactDetails = data.vatContact.email.map(_ =>
      BusinessContactDetails(data.vatContact.email.get,
        data.vatContact.daytimePhone,
        data.vatContact.mobile,
        data.vatContact.website))

    S4LVatContact(businessContactDetails = businessContactDetails)
  }

  def vatPpobFormData(data: TestSetup): S4LPpob = {
    val address: Option[ScrsAddress] = data.ppob.line1.map(_ =>
      ScrsAddress(
        line1 = data.ppob.line1.getOrElse(""),
        line2 = data.ppob.line2.getOrElse(""),
        line3 = data.ppob.line3,
        line4 = data.ppob.line4,
        postcode = data.ppob.postcode,
        country = data.ppob.country))
    S4LPpob(Some(PpobView(address.map(_.id).getOrElse(""), address)))
  }

  def vatLodgingOfficerFromData(data: TestSetup): S4LVatLodgingOfficer = {
    val homeAddress: Option[ScrsAddress] = data.officerHomeAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerHomeAddress.line1.getOrElse(""),
        line2 = data.officerHomeAddress.line2.getOrElse(""),
        line3 = data.officerHomeAddress.line3,
        line4 = data.officerHomeAddress.line4,
        postcode = data.officerHomeAddress.postcode,
        country = data.officerHomeAddress.country))

    val threeYears: Option[String] = data.officerPreviousAddress.threeYears

    val previousAddress: Option[ScrsAddress] = data.officerPreviousAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerPreviousAddress.line1.getOrElse(""),
        line2 = data.officerPreviousAddress.line2.getOrElse(""),
        line3 = data.officerPreviousAddress.line3,
        line4 = data.officerPreviousAddress.line4,
        postcode = data.officerPreviousAddress.postcode,
        country = data.officerPreviousAddress.country))

    val dob: Option[LocalDate] = data.vatLodgingOfficer.dobDay.map(_ =>
      LocalDate.of(
        data.vatLodgingOfficer.dobYear.getOrElse("1900").toInt,
        data.vatLodgingOfficer.dobMonth.getOrElse("1").toInt,
        data.vatLodgingOfficer.dobDay.getOrElse("1").toInt))

    val nino = data.vatLodgingOfficer.nino

    val completionCapacity = data.vatLodgingOfficer.role.map(_ => {
      CompletionCapacity(
        name = Name(data.vatLodgingOfficer.firstname,
          data.vatLodgingOfficer.othernames,
          data.vatLodgingOfficer.surname.getOrElse("")),
        role = data.vatLodgingOfficer.role.getOrElse(""))
    })

    val contactDetails: Option[OfficerContactDetails] = data.vatLodgingOfficer.email.map(_ =>
      OfficerContactDetails(
        email = data.vatLodgingOfficer.email,
        mobile = data.vatLodgingOfficer.mobile,
        tel = data.vatLodgingOfficer.phone))

    val formerName: Option[FormerNameView] = data.vatLodgingOfficer.formernameChoice.collect {
      case "true" => FormerNameView(true, data.vatLodgingOfficer.formername)
      case "false" => FormerNameView(false, None)
    }

    val formerNameDate: Option[LocalDate] = data.vatLodgingOfficer.formernameChangeDay.map(_ =>
      LocalDate.of(
        data.vatLodgingOfficer.formernameChangeYear.getOrElse("1900").toInt,
        data.vatLodgingOfficer.formernameChangeMonth.getOrElse("1").toInt,
        data.vatLodgingOfficer.formernameChangeDay.getOrElse("1").toInt))

    S4LVatLodgingOfficer(
      previousAddress = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      officerHomeAddress = homeAddress.map(a => OfficerHomeAddressView(a.id, Some(a))),
      officerDateOfBirth = dob.map(OfficerDateOfBirthView(_, completionCapacity.map(_.name))),
      officerNino = nino.map(OfficerNinoView(_)),
      completionCapacity = completionCapacity.map(CompletionCapacityView(_)),
      officerContactDetails = contactDetails.map(OfficerContactDetailsView(_)),
      formerName = formerName,
      formerNameDate = formerNameDate.map(FormerNameDateView(_))
    )
  }

  def vatFrsFromData(data: TestSetup): S4LFlatRateScheme = {

    val joinFrs: Option[String] = data.vatFlatRateScheme.joinFrs
    val annualCostsInclusive: Option[String] = data.vatFlatRateScheme.annualCostsInclusive
    val annualCostsLimited: Option[String] = data.vatFlatRateScheme.annualCostsLimited
    val registerForFrs: Option[String] = data.vatFlatRateScheme.registerForFrs

    val frsStartDate = data.vatFlatRateScheme.frsStartDateChoice match {
      case None => FrsStartDateView()
      case Some("DIFFERENT_DATE") => FrsStartDateView(dateType = "DIFFERENT_DATE", date = Some(LocalDate.of(
        data.vatFlatRateScheme.frsStartDateYear.map(_.toInt).get,
        data.vatFlatRateScheme.frsStartDateMonth.map(_.toInt).get,
        data.vatFlatRateScheme.frsStartDateDay.map(_.toInt).get
      )))

      case Some(t) => FrsStartDateView(t, None)
    }

    S4LFlatRateScheme(
      joinFrs = joinFrs.map(a => JoinFrsView(a.toBoolean)),
      frsStartDate = Some(frsStartDate),
      annualCostsInclusive = annualCostsInclusive.map(AnnualCostsInclusiveView(_)),
      annualCostsLimited = annualCostsLimited.map(AnnualCostsLimitedView(_)),
      registerForFrs = registerForFrs.map(a => RegisterForFrsView(a.toBoolean))
    )

  }

}
