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
import models.api._
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.cultural.NotForProfit.NOT_PROFIT_YES
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.TemporaryContracts.TEMP_CONTRACTS_NO
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatFinancials.ZeroRatedSales.ZERO_RATED_SALES_YES
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class S4LModelsSpec  extends UnitSpec with Inspectors with VatRegistrationFixture {

  "S4LVatFinancials.S4LApiTransformer.toApi" should {

    val s4l = S4LVatFinancials(
      zeroRatedTurnover = Some(ZeroRatedSales(ZERO_RATED_SALES_YES)),
      zeroRatedTurnoverEstimate = Some(EstimateZeroRatedSales(1))
    )

    "transform complete S4L model to API" in {
      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4l) shouldBe expected
    }

    "transform valid partial S4L model to API" in {
      val s4lWithoutAccountingPeriod = s4l.copy()

      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4lWithoutAccountingPeriod) shouldBe expected
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
        frsStartDate = Some(FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(specificDate))),
        categoryOfBusiness = Some(BusinessSectorView(category, percent))
      )

      val expected = VatFlatRateScheme(
        joinFrs = true,
        annualCostsInclusive = Some(AnnualCostsInclusiveView.NO),
        annualCostsLimited = Some(AnnualCostsLimitedView.NO),
        doYouWantToUseThisRate = Some(true),
        whenDoYouWantToJoinFrs = Some(FrsStartDateView.DIFFERENT_DATE),
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
}
