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

package controllers.builders

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.SummaryRow
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency

class SummaryTaxableSalesSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a company details section" should {

    val bankAccount = VatBankAccount(accountNumber = "12345678", accountName = "Account Name", accountSortCode = testSortCode)

    "with estimatedSalesValueRow render" should {

      "a £0 value should be returned as an estimated sales with an empty vat financials" in {
        val builder = SummaryTaxableSalesSectionBuilder()
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "taxableSales.estimatedSalesValue",
            "£0",
            Some(controllers.vatFinancials.routes.EstimateVatTurnoverController.show())
          )
      }

      "a real value should be returned as an estimated sales with vat financials containing a turnover estimate" in {
        val financials = VatFinancials(
          turnoverEstimate = 15000000L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false
        )
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials))
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "taxableSales.estimatedSalesValue",
            "£15000000",
            Some(controllers.vatFinancials.routes.EstimateVatTurnoverController.show())
          )
      }
    }

    "with zeroRatedSalesRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryTaxableSalesSectionBuilder()
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "taxableSales.zeroRatedSales",
            "app.common.no",
            Some(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
          )
      }

      "a 'Yes' value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = Some(10000L)
        )
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials))
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "taxableSales.zeroRatedSales",
            "app.common.yes",
            Some(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
          )
      }
    }

    "with estimatedZeroRatedSalesRow render" should {

      "an empty value should be returned with an empty vat financials" in {
        val builder = SummaryTaxableSalesSectionBuilder()
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "taxableSales.zeroRatedSalesValue",
            "£",
            Some(controllers.vatFinancials.routes.EstimateZeroRatedSalesController.show())
          )
      }

      "a real value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = Some(10000L)
        )
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials))
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "taxableSales.zeroRatedSalesValue",
            "£10000",
            Some(controllers.vatFinancials.routes.EstimateZeroRatedSalesController.show())
          )
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val financials = VatFinancials(
          turnoverEstimate = 50000L,
          accountingPeriods = VatAccountingPeriod(VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = Some(10000L),
          bankAccount = None
        )
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials))
        builder.section.id mustBe "taxableSales"
        builder.section.rows.length mustEqual 3
      }
    }

  }
}