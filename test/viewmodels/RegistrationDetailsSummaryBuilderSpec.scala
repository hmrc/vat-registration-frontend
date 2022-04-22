/*
 * Copyright 2022 HM Revenue & Customs
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
import models.api.returns.{Annual, Monthly}
import models.api.{NETP, UkCompany}
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowSeq, optSummaryListRowString}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class RegistrationDetailsSummaryBuilderSpec extends VatRegSpec {

  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    val Builder = new RegistrationDetailsSummaryBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService, govukSummaryList)
  }

  object TestContent {
    val startDate = "VAT start date"
    val mandatoryStartDate = "The date the company is registered with Companies House"
    val accountingPeriod = "When do you want to submit VAT Returns?"
    val paymentFrequency = "How often do you want to make payments?"
    val paymentMethod = "How do you want to pay VAT?"
    val bankAccount = "Have a business bank account set up"
    val bankAccountDetails = "Bank details"
    val bankAccountReason = "Reason the bank account is not set up yet"
    val joinFrs = "Do you want to join the Flat Rate Scheme?"
    val costsInclusive = "Will the business spend more than £250 over the next 3 months on ’relevant goods’?"
    val estimatedTotalSales = "Total sales, including VAT, for the next 3 months"
    val costsLimited = "Will the business spend more than £0, including VAT, on relevant goods over the next 3 months?"
    val flatRate = "Do you want to use the 3.14% flat rate?"
    val businessSector = "Business type for the Flat Rate Scheme"
    val flatRateDate = "When do you want to join the Flat Rate Scheme?"
    val flatRateRegDate = "Date of registration"
  }

  "Generate Registration Details Builder" when {
    "returns a registration detail summary list for UK company" in new Setup {
      val testVatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = UkCompany
        )),
        returns = Some(validReturns.copy(
          zeroRatedSupplies = None,
          reclaimVatOnMostReturns = Some(true),
          returnsFrequency = Some(Annual),
          annualAccountingDetails = Some(validAasDetails),
          startDate = frsDate.flatMap(_.date)
        )),
        bankAccount = Some(validUkBankAccount),
        flatRateScheme = Some(validFlatRate)
      )

      val expectedSummaryList = SummaryList(List(
        optSummaryListRowString(
          questionId = TestContent.startDate,
          optAnswer = Some("10 October 2017"),
          optUrl = Some(controllers.returns.routes.VatRegStartDateResolverController.resolve.url)),
        optSummaryListRowString(
          questionId = TestContent.accountingPeriod,
          optAnswer = Some("I would like to join the Annual Accounting Scheme"),
          optUrl = Some(controllers.returns.routes.ReturnsController.accountPeriodsPage.url)),
        optSummaryListRowString(
          questionId = TestContent.paymentFrequency,
          optAnswer = Some("Quarterly"),
          optUrl = Some(controllers.returns.routes.PaymentFrequencyController.show.url)),
        optSummaryListRowString(
          questionId = TestContent.paymentMethod,
          optAnswer = Some("BACS or internet banking"),
          optUrl = Some(controllers.returns.routes.PaymentMethodController.show.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.bankAccount,
          optAnswer = Some(true),
          optUrl = Some(controllers.bankdetails.routes.HasBankAccountController.show.url)),
        optSummaryListRowSeq(
          questionId = TestContent.bankAccountDetails,
          optAnswers = Some(Seq("testName", "12-34-56", "12345678")),
          optUrl = Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.joinFrs,
          optAnswer = Some(true),
          optUrl = Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.costsInclusive,
          optAnswer = Some(true),
          optUrl = Some(controllers.flatratescheme.routes.FlatRateController.annualCostsInclusivePage.url)),
        optSummaryListRowString(
          questionId = TestContent.estimatedTotalSales,
          optAnswer = Some("£5,003"),
          optUrl = Some(controllers.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.costsLimited,
          optAnswer = Some(true),
          optUrl = Some(controllers.flatratescheme.routes.FlatRateController.annualCostsLimitedPage.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.flatRate,
          optAnswer = Some(true),
          optUrl = Some(controllers.flatratescheme.routes.FlatRateController.yourFlatRatePage.url)),
        optSummaryListRowString(
          questionId = TestContent.businessSector,
          optAnswer = Some("Pubs"),
          optUrl = Some(controllers.flatratescheme.routes.ChooseBusinessTypeController.show.url)),
        optSummaryListRowString(
          questionId = TestContent.flatRateDate,
          optAnswer = Some(TestContent.flatRateRegDate),
          optUrl = Some(controllers.flatratescheme.routes.StartDateController.show.url))
      ).flatten)

      when(mockConfigConnector.getBusinessTypeDetails(any())).thenReturn(("Pubs", BigDecimal("6.5")))

      val res = Builder.build(testVatScheme)

      res mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "returns a registration detail summary list for NETP" in new Setup {
      val testVatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = NETP
        )),
        returns = Some(validReturns.copy(
          zeroRatedSupplies = None,
          reclaimVatOnMostReturns = Some(true),
          returnsFrequency = Some(Monthly))
        ),
        bankAccount = Some(validUkBankAccount)
      )

      val expectedSummaryList = SummaryList(List(
        optSummaryListRowString(
          questionId = TestContent.startDate,
          optAnswer = Some("10 October 2017"),
          optUrl = Some(controllers.returns.routes.VatRegStartDateResolverController.resolve.url)),
        optSummaryListRowString(
          questionId = TestContent.accountingPeriod,
          optAnswer = Some("Once a month"),
          optUrl = Some(controllers.returns.routes.ReturnsController.accountPeriodsPage.url)),
        optSummaryListRowBoolean(
          questionId = TestContent.bankAccount,
          optAnswer = Some(true),
          optUrl = Some(controllers.bankdetails.routes.HasBankAccountController.show.url))
      ).flatten)

      val res = Builder.build(testVatScheme)

      res mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }
  }

}