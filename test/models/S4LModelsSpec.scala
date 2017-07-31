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

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api._
import models.view.frs.FrsStartDateView.DIFFERENT_DATE
import models.view.frs._
import models.view.ppob.PpobView
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.cultural.NotForProfit.NOT_PROFIT_YES
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.TemporaryContracts.TEMP_CONTRACTS_NO
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_YES
import models.view.vatFinancials.ZeroRatedSales.ZERO_RATED_SALES_YES
import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod.FEB_MAY_AUG_NOV
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.{MONTHLY, QUARTERLY}
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.TradingNameView.{TRADING_NAME_NO, TRADING_NAME_YES}
import models.view.vatTradingDetails.vatChoice.StartDateView.{BUSINESS_START_DATE, SPECIFIC_DATE}
import models.view.vatTradingDetails.vatChoice.TaxableTurnover.{TAXABLE_NO, TAXABLE_YES}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.{REGISTER_NO, REGISTER_YES}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason.INTENDS_TO_SELL
import models.view.vatTradingDetails.vatChoice._
import models.view.vatTradingDetails.vatEuTrading.ApplyEori.APPLY_EORI_YES
import models.view.vatTradingDetails.vatEuTrading.EuGoods.{EU_GOODS_NO, EU_GOODS_YES}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class S4LModelsSpec  extends UnitSpec with Inspectors with VatRegistrationFixture {

  "S4LVatFinancials.S4LApiTransformer.toApi" should {

    "transform complete S4L model to API" in {
      val s4l = S4LVatFinancials(
        estimateVatTurnover = Some(EstimateVatTurnover(10)),
        zeroRatedTurnover = Some(ZeroRatedSales(ZERO_RATED_SALES_YES)),
        zeroRatedTurnoverEstimate = Some(EstimateZeroRatedSales(1)),
        vatChargeExpectancy = Some(VatChargeExpectancy(VAT_CHARGE_YES)),
        vatReturnFrequency = Some(VatReturnFrequency(QUARTERLY)),
        accountingPeriod = Some(AccountingPeriod(FEB_MAY_AUG_NOV)),
        companyBankAccount = Some(CompanyBankAccount(COMPANY_BANK_ACCOUNT_YES)),
        companyBankAccountDetails = Some(CompanyBankAccountDetails(
          accountName = "name", sortCode = "code", accountNumber = "number"))
      )
      val expected = VatFinancials(
        bankAccount = Some(VatBankAccount(
          accountName = "name", accountSortCode = "code", accountNumber = "number")),
        turnoverEstimate = 10,
        zeroRatedTurnoverEstimate = Some(1),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(QUARTERLY, Some(FEB_MAY_AUG_NOV.toLowerCase))
      )

      S4LVatFinancials.apiT.toApi(s4l, VatFinancials.empty) shouldBe expected
    }

    "transform S4L model with changes to API" in {
      val s4l = S4LVatFinancials(vatReturnFrequency = None) //Some(VatReturnFrequency(MONTHLY)))

      val before = VatFinancials(
        bankAccount = Some(VatBankAccount(
          accountName = "oldname", accountSortCode = "oldcode", accountNumber = "oldnumber")),
        turnoverEstimate = 99,
        zeroRatedTurnoverEstimate = Some(9),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(frequency = QUARTERLY, periodStart = Some(FEB_MAY_AUG_NOV))
      )

      val expected = VatFinancials(
        bankAccount = None,
        turnoverEstimate = 99,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(frequency = MONTHLY, periodStart = None)
      )

      S4LVatFinancials.apiT.toApi(s4l, before) shouldBe expected
    }
  }

  "S4LTradingDetails.S4LModelTransformer.toS4LModel" should {
    val specificDate = LocalDate.of(2017, 11, 12)
    val tradingName = "name"

    "transform VatScheme to S4L container" in {
      val vs = emptyVatScheme.copy(
        tradingDetails = Some(VatTradingDetails(
          vatChoice = VatChoice(
            necessity = NECESSITY_VOLUNTARY,
            vatStartDate = VatStartDate(selection = SPECIFIC_DATE, startDate = Some(specificDate)),
            reason = Some(INTENDS_TO_SELL),
            vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)),
          tradingName = TradingName(selection = true, tradingName = Some(tradingName)),
          euTrading = VatEuTrading(selection = true, eoriApplication = Some(true))
        ))
      )

      val expected = S4LTradingDetails(
        taxableTurnover = Some(TaxableTurnover(TAXABLE_NO)),
        tradingName = Some(TradingNameView(yesNo = TRADING_NAME_YES, tradingName = Some(tradingName))),
        startDate = Some(StartDateView(
          dateType = SPECIFIC_DATE,
          date = Some(specificDate),
          ctActiveDate = None)),
        voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
        voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(INTENDS_TO_SELL)),
        euGoods = Some(EuGoods(EU_GOODS_YES)),
        applyEori = Some(ApplyEori(APPLY_EORI_YES)),
        overThreshold = Some(OverThresholdView(false, None))
      )

      S4LTradingDetails.modelT.toS4LModel(vs) shouldBe expected
    }
  }

  "S4LTradingDetails.S4LApiTransformer.toApi" should {
    val specificDate = LocalDate.of(2017, 11, 12)
    val tradingName = "name"

    "transform complete S4L model to API" in {
      val s4l = S4LTradingDetails(
        taxableTurnover = Some(TaxableTurnover(TAXABLE_NO)),
        tradingName = Some(TradingNameView(yesNo = TRADING_NAME_YES, tradingName = Some(tradingName))),
        startDate = Some(StartDateView(
          dateType = SPECIFIC_DATE,
          date = Some(specificDate),
          ctActiveDate = None)),
        voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
        voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(INTENDS_TO_SELL)),
        euGoods = Some(EuGoods(EU_GOODS_YES)),
        applyEori = Some(ApplyEori(APPLY_EORI_YES)),
        overThreshold = Some(OverThresholdView(false, None))
      )

      val expected = VatTradingDetails(
        vatChoice = VatChoice(
          necessity = NECESSITY_VOLUNTARY,
          vatStartDate = VatStartDate(selection = SPECIFIC_DATE, startDate = Some(specificDate)),
          reason = Some(INTENDS_TO_SELL),
          vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)),
        tradingName = TradingName(selection = true, tradingName = Some(tradingName)),
        euTrading = VatEuTrading(selection = true, eoriApplication = Some(true))
      )

      S4LTradingDetails.apiT.toApi(s4l, VatTradingDetails.empty) shouldBe expected
    }

    "transform S4L model with changes to API" in {
      val ctDate = LocalDate.of(2016, 10, 11)

      val s4l = S4LTradingDetails(
        taxableTurnover = Some(TaxableTurnover(TAXABLE_YES)),
        tradingName = Some(TradingNameView(yesNo = TRADING_NAME_NO, tradingName = None)),
        startDate = Some(StartDateView(
          dateType = BUSINESS_START_DATE,
          date = None,
          ctActiveDate = Some(ctDate))),
        voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_NO)),
        voluntaryRegistrationReason = None,
        euGoods = Some(EuGoods(EU_GOODS_NO)),
        applyEori = None,
        overThreshold = Some(OverThresholdView(true, Some(testDate)))
      )

      val before = VatTradingDetails(
        vatChoice = VatChoice(
          necessity = NECESSITY_VOLUNTARY,
          vatStartDate = VatStartDate(selection = SPECIFIC_DATE, startDate = Some(specificDate)),
          reason = Some(INTENDS_TO_SELL),
          vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)),
        tradingName = TradingName(selection = true, tradingName = Some(tradingName)),
        euTrading = VatEuTrading(selection = true, eoriApplication = Some(true))
      )

      val expected = VatTradingDetails(
        vatChoice = VatChoice(
          necessity = NECESSITY_OBLIGATORY,
          vatStartDate = VatStartDate(selection = BUSINESS_START_DATE, startDate = Some(ctDate)),
          reason = None,
          vatThresholdPostIncorp = Some(validVatThresholdPostIncorp.copy(overThresholdSelection = true, overThresholdDate = Some(testDate)))),
        tradingName = TradingName(selection = false, tradingName = None),
        euTrading = VatEuTrading(selection = false, eoriApplication = None)
      )

      S4LTradingDetails.apiT.toApi(s4l, before) shouldBe expected
    }
  }

  "S4LVatSicAndCompliance.S4LApiTransformer.toApi" should {
    val sicCode = SicCode("a", "b", "c")
    val description = "bad"

    "transform cultural S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription(description)),
        mainBusinessActivity = Some(MainBusinessActivityView(id = "id", mainBusinessActivity = Some(sicCode))),
        // cultural
        notForProfit = Some(NotForProfit(NOT_PROFIT_YES))
      )

      val expected = VatSicAndCompliance(
        businessDescription = description,
        mainBusinessActivity = sicCode,
        culturalCompliance = Some(VatComplianceCultural(notForProfit = true))
      )

      S4LVatSicAndCompliance.apiT.toApi(s4l, VatSicAndCompliance.empty) shouldBe expected
    }

    "transform labour S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription(description)),
        mainBusinessActivity = Some(MainBusinessActivityView(id = "id", mainBusinessActivity = Some(sicCode))),
        // labour
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_NO)),
        skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
      )

      val expected = VatSicAndCompliance(
        businessDescription = description,
        mainBusinessActivity = sicCode,
        labourCompliance = Some(VatComplianceLabour(
          labour = true,
          workers = Some(8),
          temporaryContracts = Some(false),
          skilledWorkers = Some(true)
        ))
      )

      S4LVatSicAndCompliance.apiT.toApi(s4l, VatSicAndCompliance.empty) shouldBe expected
    }

    "transform finance S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription(description)),
        mainBusinessActivity = Some(MainBusinessActivityView(id = "id", mainBusinessActivity = Some(sicCode))),
        // finance
        adviceOrConsultancy = Some(AdviceOrConsultancy(true)),
        actAsIntermediary = Some(ActAsIntermediary(false)),
        chargeFees = Some(ChargeFees(false)),
        leaseVehicles = Some(LeaseVehicles(false)),
        additionalNonSecuritiesWork = Some(AdditionalNonSecuritiesWork(false)),
        discretionaryInvestmentManagementServices = Some(DiscretionaryInvestmentManagementServices(false)),
        investmentFundManagement = Some(InvestmentFundManagement(false)),
        manageAdditionalFunds = Some(ManageAdditionalFunds(false))
      )

      val expected = VatSicAndCompliance(
        businessDescription = description,
        mainBusinessActivity = sicCode,
        financialCompliance = Some(VatComplianceFinancial(
          adviceOrConsultancyOnly = true,
          actAsIntermediary = false,
          chargeFees = Some(false),
          additionalNonSecuritiesWork = Some(false),
          discretionaryInvestmentManagementServices = Some(false),
          vehicleOrEquipmentLeasing = Some(false),
          investmentFundManagementServices = Some(false),
          manageFundsAdditional = Some(false)
        ))
      )

      S4LVatSicAndCompliance.apiT.toApi(s4l, VatSicAndCompliance.empty) shouldBe expected
    }


  }

  "S4LPpob.S4LModelTransformer.toS4LModel" should {
    "transform API to S4L model" in {
      val address = ScrsAddress(line1 = "l1", line2 = "l2", postcode = Some("postcode"))
      val vs = emptyVatScheme.copy(ppob = Some(address))

      val expected = S4LPpob(address = Some(PpobView(address.id, Some(address))))

      S4LPpob.modelT.toS4LModel(vs) shouldBe expected
    }
  }

  "S4LFlatRateScheme.S4LApiTransformer.toApi" should {
    val specificDate = LocalDate.of(2017, 11, 12)
    val category = "category"
    val percent = 16.5

    "transform complete s4l container to API" in {

      val s4l = S4LFlatRateScheme(
        joinFrs = Some(JoinFrsView(true)),
        annualCostsInclusive = Some(AnnualCostsInclusiveView(AnnualCostsInclusiveView.NO)),
        annualCostsLimited = Some(AnnualCostsLimitedView(AnnualCostsLimitedView.NO)),
        registerForFrs = Some(RegisterForFrsView(true)),
        frsStartDate = Some(FrsStartDateView(DIFFERENT_DATE, Some(specificDate))),
        categoryOfBusiness = Some(BusinessSectorView(category, percent))
      )

      val expected = VatFlatRateScheme(
        joinFrs = true,
        annualCostsInclusive = Some(AnnualCostsInclusiveView.NO),
        annualCostsLimited = Some(AnnualCostsLimitedView.NO),
        doYouWantToUseThisRate = Some(true),
        whenDoYouWantToJoinFrs = Some(DIFFERENT_DATE),
        startDate = Some(specificDate),
        categoryOfBusiness = Some(category),
        percentage = Some(percent)
      )

      S4LFlatRateScheme.apiT.toApi(s4l, VatFlatRateScheme()) shouldBe expected
    }

    "transform s4l container with changes to API" in {

      val s4l = S4LFlatRateScheme(
        joinFrs = None
      )

      val before = VatFlatRateScheme(
        joinFrs = true,
        annualCostsInclusive = Some(AnnualCostsInclusiveView.NO),
        annualCostsLimited = Some(AnnualCostsLimitedView.NO),
        doYouWantToUseThisRate = Some(true),
        whenDoYouWantToJoinFrs = Some(DIFFERENT_DATE),
        startDate = Some(specificDate),
        categoryOfBusiness = Some(category),
        percentage = Some(percent)
      )

      val expected = VatFlatRateScheme(
        joinFrs = false
      )

      S4LFlatRateScheme.apiT.toApi(s4l, before) shouldBe expected
    }

  }

  "S4LVatContact.S4LModelTransformer.toS4LModel" should {
    "transform API to S4L model" in {
      val vs = emptyVatScheme.copy(vatContact = Some(
        VatContact(digitalContact = VatDigitalContact(email = "email", tel = Some("tel"), mobile = Some("mobile")), website = Some("website"))))

      val expected = S4LVatContact(businessContactDetails = Some(BusinessContactDetails(
        email = "email", daytimePhone = Some("tel"), mobile = Some("mobile"), website = Some("website")
      )))

      S4LVatContact.modelT.toS4LModel(vs) shouldBe expected
    }
  }

  "S4LVatLodgingOfficer.S4LApiTransformer.toApi" should {
    val address = ScrsAddress(line1 = "l1", line2 = "l2", postcode = Some("postcode"))
    val prevAddress = ScrsAddress(line1 = "pal1", line2 = "pal2", postcode = Some("paPostcode"))
    val date = LocalDate.of(2017, 11, 12)
    val name = Name(forename = Some("first"), otherForenames = None, surname = "surname", title = None)
    val testNino = "nino"
    val testRole = "role"

    "transform complete s4l container to API" in {

      val s4l = S4LVatLodgingOfficer(
        officerHomeAddress = Some(OfficerHomeAddressView(address.id, Some(address))),
        officerSecurityQuestions = Some(OfficerSecurityQuestionsView(dob = date, nino = testNino, officerName = Some(name))),
        completionCapacity = Some(CompletionCapacityView(id = "id", completionCapacity = Some(CompletionCapacity(name, testRole)))),
        officerContactDetails = Some(
          OfficerContactDetailsView(email = Some("email"), daytimePhone = Some("daytimePhone"), mobile = Some("mobile"))),
        formerName = Some(FormerNameView(yesNo = true, formerName = Some("formerName"))),
        formerNameDate = Some(FormerNameDateView(date)),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      val expected = VatLodgingOfficer(
        currentAddress = address,
        dob = DateOfBirth(date),
        nino = testNino,
        role = testRole,
        name = name,
        changeOfName = ChangeOfName(nameHasChanged = true,
          formerName = Some(FormerName(formerName = "formerName", dateOfNameChange = Some(date)))),
        currentOrPreviousAddress = CurrentOrPreviousAddress(currentAddressThreeYears = false, previousAddress = Some(prevAddress)),
        contact = OfficerContactDetails(Some("email"), Some("daytimePhone"), Some("mobile"))
      )

      S4LVatLodgingOfficer.apiT.toApi(s4l, VatLodgingOfficer.empty) shouldBe expected
    }

    "transform s4l container with changes to API" in {

      val s4l = S4LVatLodgingOfficer(
        officerHomeAddress = Some(OfficerHomeAddressView(address.id, Some(address))),
        officerSecurityQuestions = None,
        completionCapacity = Some(CompletionCapacityView(id = "id", completionCapacity = Some(CompletionCapacity(name, testRole)))),
        officerContactDetails = Some(
          OfficerContactDetailsView(email = Some("email"), daytimePhone = Some("daytimePhone"), mobile = Some("mobile"))),
        formerName = Some(FormerNameView(yesNo = true, formerName = Some("formerName"))),
        formerNameDate = Some(FormerNameDateView(date)),
        previousAddress = Some(PreviousAddressView(true, None))
      )

      val before = VatLodgingOfficer(
        currentAddress = address,
        dob = DateOfBirth(date),
        nino = testNino,
        role = testRole,
        name = name,
        changeOfName = ChangeOfName(nameHasChanged = true,
          formerName = Some(FormerName(formerName = "formerName", dateOfNameChange = Some(date)))),
        currentOrPreviousAddress = CurrentOrPreviousAddress(currentAddressThreeYears = false, previousAddress = Some(prevAddress)),
        contact = OfficerContactDetails(Some("email"), Some("daytimePhone"), Some("mobile"))
      )

      val expected = VatLodgingOfficer(
        currentAddress = address,
        dob = DateOfBirth(date),
        nino = testNino,
        role = testRole,
        name = name,
        changeOfName = ChangeOfName(nameHasChanged = true,
          formerName = Some(FormerName(formerName = "formerName", dateOfNameChange = Some(date)))),
        currentOrPreviousAddress = CurrentOrPreviousAddress(currentAddressThreeYears = true, previousAddress = None),
        contact = OfficerContactDetails(Some("email"), Some("daytimePhone"), Some("mobile"))
      )

      S4LVatLodgingOfficer.apiT.toApi(s4l, before) shouldBe expected
    }

  }

}