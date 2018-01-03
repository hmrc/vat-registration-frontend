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

package models

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api.VatEligibilityChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api._
import models.view.frs.FrsStartDateView.DIFFERENT_DATE
import models.view.frs._
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.cultural.NotForProfit.NOT_PROFIT_YES
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.TemporaryContracts.TEMP_CONTRACTS_NO
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
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
import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
import models.view.vatTradingDetails.vatChoice.StartDateView.SPECIFIC_DATE
import models.view.vatTradingDetails.vatChoice.TaxableTurnover.TAXABLE_NO
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.{REGISTER_NO, REGISTER_YES}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason.INTENDS_TO_SELL
import models.view.vatTradingDetails.vatChoice._
import models.view.vatTradingDetails.vatEuTrading.ApplyEori.APPLY_EORI_YES
import models.view.vatTradingDetails.vatEuTrading.EuGoods.EU_GOODS_YES
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class S4LModelsSpec  extends UnitSpec with Inspectors with VatRegistrationFixture {

  "S4LVatFinancials.S4LApiTransformer.toApi" should {

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

    "transform complete S4L model to API" in {
      val expected = VatFinancials(
        bankAccount = Some(VatBankAccount(
          accountName = "name", accountSortCode = "code", accountNumber = "number")),
        turnoverEstimate = 10,
        zeroRatedTurnoverEstimate = Some(1),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(QUARTERLY, Some(FEB_MAY_AUG_NOV.toLowerCase))
      )

      S4LVatFinancials.apiT.toApi(s4l) shouldBe expected
    }

    "transform valid partial S4L model to API" in {
      val s4lWithoutAccountingPeriod = s4l.copy(
        vatReturnFrequency = None,
        accountingPeriod = None)

      val expected = VatFinancials(
        bankAccount = Some(VatBankAccount(
          accountName = "name", accountSortCode = "code", accountNumber = "number")),
        turnoverEstimate = 10,
        zeroRatedTurnoverEstimate = Some(1),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(MONTHLY, None)
      )

      S4LVatFinancials.apiT.toApi(s4lWithoutAccountingPeriod) shouldBe expected
    }

    "transform S4L model with incomplete data error" in {
      val s4lNoTurnover = s4l.copy(estimateVatTurnover = None)
      an[IllegalStateException] should be thrownBy S4LVatFinancials.apiT.toApi(s4lNoTurnover)

      val s4lNoVatChargeExpectancy = s4l.copy(vatChargeExpectancy = None)
      an[IllegalStateException] should be thrownBy S4LVatFinancials.apiT.toApi(s4lNoVatChargeExpectancy)
    }
  }

  "S4LTradingDetails.S4LModelTransformer.toS4LModel" should {
    val specificDate = LocalDate.of(2017, 11, 12)
    val tradingName = "name"

    "transform VatScheme to S4L container" in {
      val vs = emptyVatScheme.copy(
        tradingDetails = Some(VatTradingDetails(
          vatChoice = VatChoice(
            vatStartDate = VatStartDate(selection = SPECIFIC_DATE, startDate = Some(specificDate))
          ),
          tradingName = TradingName(selection = true, tradingName = Some(tradingName)),
          euTrading = VatEuTrading(selection = true, eoriApplication = Some(true))
        ))
      )

      val expected = S4LTradingDetails(
        tradingName = Some(TradingNameView(yesNo = TRADING_NAME_YES, tradingName = Some(tradingName))),
        startDate = Some(StartDateView(
          dateType = SPECIFIC_DATE,
          date = Some(specificDate),
          ctActiveDate = None)),
        euGoods = Some(EuGoods(EU_GOODS_YES)),
        applyEori = Some(ApplyEori(APPLY_EORI_YES))
      )

      S4LTradingDetails.modelT.toS4LModel(vs) shouldBe expected
    }
  }

  "S4LTradingDetails.S4LApiTransformer.toApi" should {
    val specificDate = LocalDate.of(2017, 11, 12)
    val tradingName = "name"

    val s4l = S4LTradingDetails(
      tradingName = Some(TradingNameView(yesNo = TRADING_NAME_YES, tradingName = Some(tradingName))),
      startDate = Some(StartDateView(
        dateType = SPECIFIC_DATE,
        date = Some(specificDate),
        ctActiveDate = None)),
      euGoods = Some(EuGoods(EU_GOODS_YES)),
      applyEori = Some(ApplyEori(APPLY_EORI_YES))
    )

    "transform complete S4L with voluntary registration model to API" in {
      val expected = VatTradingDetails(
        vatChoice = VatChoice(
          vatStartDate = VatStartDate(selection = SPECIFIC_DATE, startDate = Some(specificDate))
        ),
        tradingName = TradingName(selection = true, tradingName = Some(tradingName)),
        euTrading = VatEuTrading(selection = true, eoriApplication = Some(true))
      )

      S4LTradingDetails.apiT.toApi(s4l) shouldBe expected
    }

    "transform S4L model with incomplete data error" in {
      val s4lNoStartDate = s4l.copy(startDate = None)
      an[IllegalStateException] should be thrownBy S4LTradingDetails.apiT.toApi(s4lNoStartDate)

      val s4lNoTradingName = s4l.copy(tradingName = None)
      an[IllegalStateException] should be thrownBy S4LTradingDetails.apiT.toApi(s4lNoTradingName)

      val s4lNoEuGoods = s4l.copy(euGoods = None)
      an[IllegalStateException] should be thrownBy S4LTradingDetails.apiT.toApi(s4lNoEuGoods)

    }
  }

  "S4LVatSicAndCompliance.S4LApiTransformer.toApi" should {
    val sicCode = SicCode("a", "b", "c")
    val description = "bad"
    val mbav = MainBusinessActivityView(id = "id", mainBusinessActivity = Some(sicCode))
    val bad = BusinessActivityDescription(description)

    "transform cultural S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(bad),
        mainBusinessActivity = Some(mbav),
        // cultural
        notForProfit = Some(NotForProfit(NOT_PROFIT_YES))
      )

      val expected = VatSicAndCompliance(
        businessDescription = description,
        mainBusinessActivity = sicCode,
        culturalCompliance = Some(VatComplianceCultural(notForProfit = true))
      )

      S4LVatSicAndCompliance.apiT.toApi(s4l) shouldBe expected
    }

    "transform labour S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(bad),
        mainBusinessActivity = Some(mbav),
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

      S4LVatSicAndCompliance.apiT.toApi(s4l) shouldBe expected
    }

    "transform finance S4L model to API" in {

      val s4l = S4LVatSicAndCompliance(
        description = Some(bad),
        mainBusinessActivity = Some(mbav),
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

      S4LVatSicAndCompliance.apiT.toApi(s4l) shouldBe expected
    }

    "transform S4L model with incomplete data error" in {
      val s4lNoDescription = S4LVatSicAndCompliance(description = None, mainBusinessActivity = Some(mbav))
      an[IllegalStateException] should be thrownBy S4LVatSicAndCompliance.apiT.toApi(s4lNoDescription)

      val s4lNoMBA = S4LVatSicAndCompliance(description = Some(bad), mainBusinessActivity = None)
      an[IllegalStateException] should be thrownBy S4LVatSicAndCompliance.apiT.toApi(s4lNoMBA)
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

      S4LFlatRateScheme.apiT.toApi(s4l) shouldBe expected
    }

    "transform s4l container with defaults to API" in {
      val s4l = S4LFlatRateScheme(joinFrs = None)
      val expected = VatFlatRateScheme(joinFrs = false)

      S4LFlatRateScheme.apiT.toApi(s4l) shouldBe expected
    }

  }

  "S4LVatEligibility.S4LModelTransformer.toApi" should {
    "transform complete s4l container to API" in {
      val s4l = S4LVatEligibility(Some(validServiceEligibility()))
      S4LVatEligibility.apiT.toApi(s4l) shouldBe validServiceEligibility()
    }

    "transform s4l container with incomplete data error" in {
      val s4l = S4LVatEligibility()
      an[IllegalStateException] should be thrownBy S4LVatEligibility.apiT.toApi(s4l)
    }
  }

  "S4LVatContact.S4LModelTransformer.toApi" should {

    val s4l = S4LVatContact(
      businessContactDetails = Some(BusinessContactDetails(
        email = "email",
        daytimePhone = Some("tel"),
        mobile = Some("mobile"),
        website = Some("website"))),
      ppob = Some(PpobView(scrsAddress.id, Some(scrsAddress)))
    )

    "transform complete s4l container to API" in {

      val expected = VatContact(
        digitalContact = VatDigitalContact(
          email = "email",
          tel = Some("tel"),
          mobile = Some("mobile")),
        website = Some("website"),
        ppob = scrsAddress)

      S4LVatContact.apiT.toApi(s4l) shouldBe expected

    }

    "transform s4l container with incomplete data error" in {
      val s4lNoContactDetails = s4l.copy(businessContactDetails = None)
      an[IllegalStateException] should be thrownBy S4LVatContact.apiT.toApi(s4lNoContactDetails)

      val s4lPpob = s4l.copy(ppob = None)
      an[IllegalStateException] should be thrownBy S4LVatContact.apiT.toApi(s4lPpob)
    }
  }

  "S4LVatContact.S4LModelTransformer.toS4LModel" should {

    "transform API to S4L model" in {
      val vs = emptyVatScheme.copy(vatContact = Some(
        VatContact(
          digitalContact = VatDigitalContact(email = "email", tel = Some("tel"), mobile = Some("mobile")),
          website = Some("website"),
          ppob = scrsAddress)))

      val expected = S4LVatContact(
        businessContactDetails = Some(BusinessContactDetails(
                                      email = "email",
                                      daytimePhone = Some("tel"),
                                      mobile = Some("mobile"),
                                      website = Some("website"))),
        ppob = Some(PpobView(scrsAddress.id, Some(scrsAddress)))
      )


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

    "transform complete s4l container to API" in {

      val expected = VatLodgingOfficer(
        currentAddress = Some(address),
        dob = Some(DateOfBirth(date)),
        nino = Some(testNino),
        role = Some(testRole),
        name = Some(name),
        changeOfName =
          Some(ChangeOfName(
            nameHasChanged = true,
            formerName = Some(FormerName(formerName = "formerName", dateOfNameChange = Some(date))))),
        currentOrPreviousAddress = Some(CurrentOrPreviousAddress(currentAddressThreeYears = false, previousAddress = Some(prevAddress))),
        contact = Some(OfficerContactDetails(Some("email"), Some("daytimePhone"), Some("mobile")))
      )

      S4LVatLodgingOfficer.apiT.toApi(s4l) shouldBe expected
    }
  }

  "S4LVatEligibilityChoice.S4LModelTransformer.toS4LModel" should {
    "transform VatScheme to S4L container" in {
      val vs = emptyVatScheme.copy(
        vatServiceEligibility = Some(VatServiceEligibility(
          haveNino = Some(true),
          doingBusinessAbroad = Some(false),
          doAnyApplyToYou = Some(false),
          applyingForAnyOf = Some(false),
          applyingForVatExemption = Some(false),
          companyWillDoAnyOf = Some(false),
          vatEligibilityChoice = Some(VatEligibilityChoice(
            necessity = NECESSITY_VOLUNTARY,
            reason = Some(INTENDS_TO_SELL),
            vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)))
        ))
      )

      val expected = S4LVatEligibilityChoice(
        taxableTurnover = Some(TaxableTurnover(TAXABLE_NO)),
        voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
        voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(INTENDS_TO_SELL)),
        overThreshold = Some(OverThresholdView(false, None))
      )

      S4LVatEligibilityChoice.modelT.toS4LModel(vs) shouldBe expected
    }
  }

  "S4LVatEligibilityChoice.S4LModelTransformer.toApi" should {
    val s4l = S4LVatEligibilityChoice(
      taxableTurnover = Some(TaxableTurnover(TAXABLE_NO)),
      voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
      voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(INTENDS_TO_SELL)),
      overThreshold = Some(OverThresholdView(false, None))
    )

    "transform complete S4L with voluntary registration model to API" in {
      val expected = VatEligibilityChoice(
        necessity = NECESSITY_VOLUNTARY,
        reason = Some(INTENDS_TO_SELL),
        vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)
      )

      S4LVatEligibilityChoice.apiT.toApi(s4l) shouldBe expected
    }

    "transform complete S4L with mandatory registration model to API" in {

      val expected = VatEligibilityChoice(
        necessity = NECESSITY_OBLIGATORY,
        reason = None,
        vatThresholdPostIncorp = Some(validVatThresholdPostIncorp)
      )

      val s4lMandatoryBydefault = s4l.copy(voluntaryRegistration = None, voluntaryRegistrationReason = None)
      S4LVatEligibilityChoice.apiT.toApi(s4lMandatoryBydefault) shouldBe expected

      val s4lMandatoryExplicit = s4l.copy(voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_NO)), voluntaryRegistrationReason = None)
      S4LVatEligibilityChoice.apiT.toApi(s4lMandatoryExplicit) shouldBe expected

    }
  }

}
