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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.SummaryRow
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency

class SummaryAnnualAccountingSchemeSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a company details section" should {

    val bankAccount = VatBankAccount(accountNumber = "12345678", accountName = "Account Name", accountSortCode = testSortCode)

    "with vatChargeExpectancyRow render" should {

      "a 'No' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val builder = SummaryAnnualAccountingSchemeSectionBuilder()
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "annualAccountingScheme.reclaimMoreVat",
            "pages.summary.annualAccountingScheme.reclaimMoreVat.no",
            Some(controllers.vatFinancials.routes.VatChargeExpectancyController.show())
          )
      }

      "a 'Yes' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "annualAccountingScheme.reclaimMoreVat",
            "pages.summary.annualAccountingScheme.reclaimMoreVat.yes",
            Some(controllers.vatFinancials.routes.VatChargeExpectancyController.show())
          )
      }
    }

    "with accountingPeriodRow render" should {

      "a 'Once a month' answer should be returned when vat financials has an accounting period frequency set to monthly" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "annualAccountingScheme.accountingPeriod",
            "pages.summary.annualAccountingScheme.accountingPeriod.monthly",
            Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'January, April, July and October' answer should be returned when accounting period frequency is set to quarterly with January as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(periodStart = Some("jan_apr_jul_oct"), frequency = VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "annualAccountingScheme.accountingPeriod",
            "pages.summary.annualAccountingScheme.accountingPeriod.jan",
            Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'February, May, August and November' answer should be returned when accounting period frequency is set to quarterly with February as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(periodStart = Some("feb_may_aug_nov"), frequency = VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "annualAccountingScheme.accountingPeriod",
            "pages.summary.annualAccountingScheme.accountingPeriod.feb",
            Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'March, June, September and December' answer should be returned when accounting period frequency is set to quarterly with March as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(periodStart = Some("mar_jun_sep_dec"), frequency = VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "annualAccountingScheme.accountingPeriod",
            "pages.summary.annualAccountingScheme.accountingPeriod.mar",
            Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
          )
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = None
        )
        val builder = SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials = Some(financials))
        builder.section.id mustBe "annualAccountingScheme"
        builder.section.rows.length mustEqual 2
      }
    }

  }
}
