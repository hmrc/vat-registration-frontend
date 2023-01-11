/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import config.FrontendAppConfig
import controllers.vatapplication.{routes => vatApplicationRoutes}
import models.api.vatapplication.{Annual, Monthly, OverseasCompliance, StoringWithinUk}
import models.api.{NETP, UkCompany}
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowSeq, optSummaryListRowString}
import models.{ConditionalValue, FrsBusinessType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.HtmlFormat
import services.FlatRateService
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.MessageDateFormat

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RegistrationDetailsSummaryBuilderSpec extends VatRegSpec {

  val testTurnoverEstimate = "£100.00"
  val testZeroTurnoverEstimate = "£0.00"
  val testZeroRated = "£10,000.50"
  val testNipAmount = "Value of goods: £1.00"
  val testWarehouseNumber = "testWarehouseName"
  val testWarehouseName = "testWarehouseNumber"
  val testVrn = "testVrn"

  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    val Builder = new RegistrationDetailsSummaryBuilder(configConnector = mockConfigConnector, flatRateService = app.injector.instanceOf[FlatRateService], govukSummaryList)
  }

  object TestContent {
    val startDate = "VAT registration start date"
    val mandatoryStartDate = "The date the company is registered with Companies House"
    val accountingPeriod = "VAT Returns"
    val paymentFrequency = "Payment frequency"
    val paymentMethod = "VAT payment method"
    val bankAccount = "Bank or building society account"
    val bankAccountDetails = "Bank or building society details"
    val bankAccountReason = "Reason for no bank or building society details"
    val joinFrs = "Join Flat Rate Scheme"
    val costsInclusive = "Relevant goods over £250 in next 3 months"
    val estimatedTotalSales = "Estimated total sales, including VAT, in next 3 months"
    val costsLimited = "Relevant goods and VAT over £101 in next 3 months"
    val flatRate = "Do you want to use the 3.14% flat rate?"
    val businessSector = "Business type for Flat Rate Scheme"
    val flatRateDate = "Flat Rate Scheme start date"
    val flatRateRegDate = "Date of registration"
    val currentlyTrading = "Trading by registration date"
    val importsOrExports = "Trade VAT-taxable goods outside UK"
    val applyForEori = "EORI number needed"
    val turnoverEstimate = "VAT-taxable turnover for next 12 months"
    val zeroRated = "Zero-rated taxable goods for next 12 months"
    val sellOrMoveNip = "Sell or move Northern Irish goods in next 12 months"
    val receiveGoodsNip = "Receive goods in Northern Ireland from EU"
    val claimRefunds = "Expect VAT refunds"
    val vatExemption = "VAT exemption"
    val sendGoodsOverseas = "Goods to overseas customers"
    val sendGoodsToEu = "Goods to EU customers"
    val storingGoods = "Location for dispatch storage goods"
    val dispatchFromWarehouse = "Goods from Fulfilment House Due Diligence Scheme registered warehouse"
    val warehouseNumber = "Fulfilment Warehouse number"
    val warehouseName = "Fulfilment Warehouse business name"
    val formattedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  }

  "Generate Registration Details Builder" when {
    "returns a registration detail summary list for UK company" in new Setup {
      val testVatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = UkCompany
        )),
        vatApplication = Some(validVatApplication.copy(
          zeroRatedSupplies = None,
          claimVatRefunds = Some(true),
          returnsFrequency = Some(Annual),
          annualAccountingDetails = Some(validAasDetails),
          startDate = Some(LocalDate.now()),
          currentlyTrading = Some(true)
        )),
        bankAccount = Some(validUkBankAccount),
        flatRateScheme = Some(validFlatRate.copy(
          frsStart = Some(LocalDate.now())
        ))
      )

      val expectedSummaryList: SummaryList = SummaryList(List(
        optSummaryListRowBoolean(TestContent.importsOrExports, Some(false), Some(vatApplicationRoutes.ImportsOrExportsController.show.url)),
        optSummaryListRowBoolean(TestContent.applyForEori, Some(false), Some(vatApplicationRoutes.ApplyForEoriController.show.url)),
        optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
        optSummaryListRowBoolean(TestContent.claimRefunds, Some(true), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.bankAccount, optAnswer = Some(true), optUrl = Some(controllers.bankdetails.routes.HasBankAccountController.show.url)),
        optSummaryListRowSeq(questionId = TestContent.bankAccountDetails, optAnswers = Some(Seq("testName", "12-34-56", "12345678")), optUrl = Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)),
        optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
        optSummaryListRowBoolean(questionId = TestContent.currentlyTrading, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)),
        optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("I would like to join the Annual Accounting Scheme"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url)),
        optSummaryListRowString(questionId = TestContent.paymentFrequency, optAnswer = Some("Quarterly"), optUrl = Some(controllers.vatapplication.routes.PaymentFrequencyController.show.url)),
        optSummaryListRowString(questionId = TestContent.paymentMethod, optAnswer = Some("BACS or internet banking"), optUrl = Some(controllers.vatapplication.routes.PaymentMethodController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.joinFrs, optAnswer = Some(true), optUrl = Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.costsInclusive, optAnswer = Some(true), optUrl = Some(controllers.flatratescheme.routes.AnnualCostsInclusiveController.show.url)),
        optSummaryListRowString(questionId = TestContent.estimatedTotalSales, optAnswer = Some("£5,003.00"), optUrl = Some(controllers.flatratescheme.routes.EstimateTotalSalesController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.costsLimited, optAnswer = Some(true), optUrl = Some(controllers.flatratescheme.routes.AnnualCostsLimitedController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.flatRate, optAnswer = Some(true), optUrl = Some(controllers.flatratescheme.routes.YourFlatRateController.show.url)),
        optSummaryListRowString(questionId = TestContent.businessSector, optAnswer = Some("Pubs"), optUrl = Some(controllers.flatratescheme.routes.ChooseBusinessTypeController.show.url)),
        optSummaryListRowString(questionId = TestContent.flatRateDate, optAnswer = Some(TestContent.flatRateRegDate), optUrl = Some(controllers.flatratescheme.routes.StartDateController.show.url))
      ).flatten)

      when(mockConfigConnector.getBusinessType(any())).thenReturn(FrsBusinessType(testBusinessCategory, "Pubs", "Pubs", BigDecimal("6.5")))

      Builder.build(testVatScheme) mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "returns a registration detail summary list for NETP" in new Setup {
      val testVatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = NETP
        )),
        vatApplication = Some(validVatApplication.copy(
          zeroRatedSupplies = None,
          claimVatRefunds = Some(true),
          returnsFrequency = Some(Monthly),
          startDate = Some(LocalDate.now())
        )),
        bankAccount = None
      )

      val expectedSummaryList: SummaryList = SummaryList(List(
        optSummaryListRowString(questionId = TestContent.turnoverEstimate, optAnswer = Some("£100.00"), optUrl = Some(controllers.vatapplication.routes.TurnoverEstimateController.show.url)),
        optSummaryListRowBoolean(questionId = TestContent.claimRefunds, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.ClaimRefundsController.show.url)),
        optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
        optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
      ).flatten)

      Builder.build(testVatScheme) mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "hide the zero rated row if the user's turnover is £0" in new Setup {
      val scheme = emptyVatScheme.copy(
        business = Some(validBusiness.copy(
          otherBusinessInvolvement = Some(false),
          labourCompliance = Some(complianceWithLabour),
          businessActivities = Some(List(sicCode))
        )),
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        vatApplication = Some(validVatApplication.copy(
          currentlyTrading = Some(true),
          turnoverEstimate = Some(0),
          startDate = Some(LocalDate.now())
        )),
        bankAccount = Some(validUkBankAccount),
      )

      Builder.build(scheme) mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
        rows = List(
          optSummaryListRowBoolean(TestContent.importsOrExports, Some(false), Some(vatApplicationRoutes.ImportsOrExportsController.show.url)),
          optSummaryListRowBoolean(TestContent.applyForEori, Some(false), Some(vatApplicationRoutes.ApplyForEoriController.show.url)),
          optSummaryListRowString(TestContent.turnoverEstimate, Some(testZeroTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
          optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
          optSummaryListRowBoolean(questionId = TestContent.bankAccount, optAnswer = Some(true), optUrl = Some(controllers.bankdetails.routes.HasBankAccountController.show.url)),
          optSummaryListRowSeq(questionId = TestContent.bankAccountDetails, optAnswers = Some(Seq("testName", "12-34-56", "12345678")), optUrl = Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)),
          optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
          optSummaryListRowBoolean(questionId = TestContent.currentlyTrading, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)),
          optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
        ).flatten
      ))))
    }

    "not show the NIP compliance values if the user answered No to both questions" in new Setup {
      val scheme = emptyVatScheme.copy(
        business = Some(validBusiness.copy(
          otherBusinessInvolvement = Some(false),
          labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
        )),
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        vatApplication = Some(validVatApplication.copy(
          northernIrelandProtocol = Some(validNipCompliance.copy(
            goodsToEU = Some(ConditionalValue(false, None)),
            goodsFromEU = Some(ConditionalValue(false, None)))
          ),
          currentlyTrading = Some(true),
          startDate = Some(LocalDate.now())
        ))
      )

      Builder.build(scheme) mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
        rows = List(
          optSummaryListRowBoolean(TestContent.importsOrExports, Some(false), Some(vatApplicationRoutes.ImportsOrExportsController.show.url)),
          optSummaryListRowBoolean(TestContent.applyForEori, Some(false), Some(vatApplicationRoutes.ApplyForEoriController.show.url)),
          optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
          optSummaryListRowString(TestContent.zeroRated, Some(testZeroRated), Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)),
          optSummaryListRowSeq(TestContent.sellOrMoveNip, Some(Seq("No")), Some(vatApplicationRoutes.SellOrMoveNipController.show.url)),
          optSummaryListRowSeq(TestContent.receiveGoodsNip, Some(Seq("No")), Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)),
          optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
          optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
          optSummaryListRowBoolean(questionId = TestContent.currentlyTrading, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)),
          optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
        ).flatten
      ))))
    }

    "show the NIP compliance values if the user answered Yes to atleast one" in new Setup {
      val scheme = emptyVatScheme.copy(
        business = Some(validBusiness.copy(
          otherBusinessInvolvement = Some(false),
          labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
        )),
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        vatApplication = Some(validVatApplication.copy(
          northernIrelandProtocol = Some(validNipCompliance.copy(
            goodsToEU = Some(ConditionalValue(true, Some(BigDecimal(1)))),
            goodsFromEU = Some(ConditionalValue(false, None)))
          ),
          currentlyTrading = Some(true),
          startDate = Some(LocalDate.now())
        ))
      )

      Builder.build(scheme) mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
        rows = List(
          optSummaryListRowBoolean(TestContent.importsOrExports, Some(false), Some(vatApplicationRoutes.ImportsOrExportsController.show.url)),
          optSummaryListRowBoolean(TestContent.applyForEori, Some(false), Some(vatApplicationRoutes.ApplyForEoriController.show.url)),
          optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
          optSummaryListRowString(TestContent.zeroRated, Some(testZeroRated), Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)),
          optSummaryListRowSeq(TestContent.sellOrMoveNip, Some(Seq("Yes", "Value of goods: £1.00")), Some(vatApplicationRoutes.SellOrMoveNipController.show.url)),
          optSummaryListRowSeq(TestContent.receiveGoodsNip, Some(Seq("No")), Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)),
          optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
          optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
          optSummaryListRowBoolean(questionId = TestContent.currentlyTrading, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)),
          optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
        ).flatten
      ))))
    }
  }

  "the user is sending goods to the EU" when {
    "the user is using a dispatch warehouse" must {
      "show the overseas answers with the EU questions, dispatch questions and international address" in new Setup {
        val scheme = emptyVatScheme.copy(
          business = Some(validBusiness.copy(
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
          )),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
          vatApplication = Some(validVatApplication.copy(
            currentlyTrading = Some(true),
            northernIrelandProtocol = Some(validNipCompliance.copy(
              goodsToEU = Some(ConditionalValue(false, None)),
              goodsFromEU = Some(ConditionalValue(false, None))
            )),
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(true),
              goodsToEu = Some(true),
              storingGoodsForDispatch = Some(StoringWithinUk),
              usingWarehouse = Some(true),
              fulfilmentWarehouseNumber = Some(testWarehouseNumber),
              fulfilmentWarehouseName = Some(testWarehouseName)
            )),
            startDate = Some(LocalDate.now())
          ))
        )

        Builder.build(scheme) mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(TestContent.zeroRated, Some(testZeroRated), Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowSeq(TestContent.sellOrMoveNip, Some(Seq("No")), Some(vatApplicationRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(TestContent.receiveGoodsNip, Some(Seq("No")), Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)),
            optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowBoolean(TestContent.sendGoodsOverseas, Some(true), Some(vatApplicationRoutes.SendGoodsOverseasController.show.url)),
            optSummaryListRowBoolean(TestContent.sendGoodsToEu, Some(true), Some(vatApplicationRoutes.SendEUGoodsController.show.url)),
            optSummaryListRowString(TestContent.storingGoods, Some("Within the UK"), Some(vatApplicationRoutes.StoringGoodsController.show.url)),
            optSummaryListRowBoolean(TestContent.dispatchFromWarehouse, Some(true), Some(vatApplicationRoutes.DispatchFromWarehouseController.show.url)),
            optSummaryListRowString(TestContent.warehouseNumber, Some(testWarehouseNumber), Some(vatApplicationRoutes.WarehouseNumberController.show.url)),
            optSummaryListRowString(TestContent.warehouseName, Some(testWarehouseName), Some(vatApplicationRoutes.WarehouseNameController.show.url)),
            optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
            optSummaryListRowBoolean(questionId = TestContent.currentlyTrading, optAnswer = Some(true), optUrl = Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)),
            optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
          ).flatten
        ))))
      }
    }

    "the user is not using a dispatch warehouse" must {
      "show the overseas answers with the EU questions with an international address, minus the dispatch questions" in new Setup {
        val scheme = emptyVatScheme.copy(
          business = Some(validBusiness.copy(
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
          )),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
          vatApplication = Some(validVatApplication.copy(
            northernIrelandProtocol = Some(validNipCompliance.copy(
              goodsToEU = Some(ConditionalValue(false, None)),
              goodsFromEU = Some(ConditionalValue(false, None))
            )),
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(true),
              goodsToEu = Some(true),
              storingGoodsForDispatch = Some(StoringWithinUk),
              usingWarehouse = Some(false)
            )),
            startDate = Some(LocalDate.now())
          ))
        )

        val res = Builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(TestContent.zeroRated, Some(testZeroRated), Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowSeq(TestContent.sellOrMoveNip, Some(Seq("No")), Some(vatApplicationRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(TestContent.receiveGoodsNip, Some(Seq("No")), Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)),
            optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowBoolean(TestContent.sendGoodsOverseas, Some(true), Some(vatApplicationRoutes.SendGoodsOverseasController.show.url)),
            optSummaryListRowBoolean(TestContent.sendGoodsToEu, Some(true), Some(vatApplicationRoutes.SendEUGoodsController.show.url)),
            optSummaryListRowString(TestContent.storingGoods, Some("Within the UK"), Some(vatApplicationRoutes.StoringGoodsController.show.url)),
            optSummaryListRowBoolean(TestContent.dispatchFromWarehouse, Some(false), Some(vatApplicationRoutes.DispatchFromWarehouseController.show.url)),
            optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
            optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
          ).flatten
        ))))
      }
    }
  }

  "the user is overseas" when {
    "the user is not sending goods to the EU" must {
      "show the overseas answers with an international address, mandatory trading name, without questions regarding sending goods to the EU" in new Setup {
        val scheme = emptyVatScheme.copy(
          business = Some(validBusiness.copy(
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
          )),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(true),
              goodsToEu = Some(false)
            )),
            startDate = Some(LocalDate.now())
          ))
        )

        val res = Builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(TestContent.turnoverEstimate, Some(testTurnoverEstimate), Some(vatApplicationRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(TestContent.zeroRated, Some(testZeroRated), Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowBoolean(TestContent.claimRefunds, Some(false), Some(vatApplicationRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowBoolean(TestContent.sendGoodsOverseas, Some(true), Some(vatApplicationRoutes.SendGoodsOverseasController.show.url)),
            optSummaryListRowString(questionId = TestContent.startDate, optAnswer = Some(TestContent.formattedDate), optUrl = Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)),
            optSummaryListRowString(questionId = TestContent.accountingPeriod, optAnswer = Some("Once a month"), optUrl = Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url))
          ).flatten
        ))))
      }
    }
  }

}
