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

package controllers.builders

import features.turnoverEstimates.TurnoverEstimates
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.SummaryRow

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
            Some(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
          )
      }

      "a real value should be returned as an estimated sales containing a turnover estimate" in {
        val turnOEstimate = TurnoverEstimates(
          vatTaxable = 15000000L
        )
        val builder = SummaryTaxableSalesSectionBuilder(turnoverEstimates = Some(turnOEstimate))
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "taxableSales.estimatedSalesValue",
            "£15000000",
            Some(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
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
        val financials = VatFinancials(zeroRatedTurnoverEstimate = Some(10000L))
        val turnoverEstimates = TurnoverEstimates(0L)
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials),turnoverEstimates = Some(turnoverEstimates))
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
        val financials = VatFinancials(zeroRatedTurnoverEstimate = Some(10000L))
        val turnoverEstimates = TurnoverEstimates(0L)
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials),turnoverEstimates = Some(turnoverEstimates))
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
        val financials = VatFinancials(zeroRatedTurnoverEstimate = Some(10000L))
        val turnoverEstimates = TurnoverEstimates(50000L)
        val builder = SummaryTaxableSalesSectionBuilder(vatFinancials = Some(financials),turnoverEstimates = Some(turnoverEstimates))
        builder.section.id mustBe "taxableSales"
        builder.section.rows.length mustEqual 3
      }
    }

  }
}
