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

class SummaryBusinessBankDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a company details section" should {

    val bankAccount = VatBankAccount(accountNumber = "12345678", accountName = "Account Name", accountSortCode = testSortCode)

    "with companyBankAccountRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryBusinessBankDetailsSectionBuilder()
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount",
            "app.common.no",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
          )
      }

      "a 'Yes' value should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(bankAccount)
        )
        val builder = SummaryBusinessBankDetailsSectionBuilder(vatFinancials = Some(financials))
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount",
            "app.common.yes",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
          )
      }
    }

    "with companyBankAccountNameRow render" should {

      "a 'No' value should be returned with an empty bank account name in vat financials" in {
        val builder = SummaryBusinessBankDetailsSectionBuilder()
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.name",
            "app.common.no",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real name value should be returned with bank account name set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(bankAccount)
        )
        val builder = SummaryBusinessBankDetailsSectionBuilder(vatFinancials = Some(financials))
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.name",
            "Account Name",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountNumberRow render" should {

      "a 'No' value should be returned with an empty bank account number in vat financials" in {
        val builder = SummaryBusinessBankDetailsSectionBuilder()
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.number",
            "app.common.no",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real bank account number's last 4 digits the rest being masked, should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(bankAccount)
        )
        val builder = SummaryBusinessBankDetailsSectionBuilder(vatFinancials = Some(financials))
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.number",
            "****5678",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountSortCodeRow render" should {

      "a 'No' value should be returned with an empty bank account sort code in vat financials" in {
        val builder = SummaryBusinessBankDetailsSectionBuilder()
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.sortCode",
            "app.common.no",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real sort code value should be returned with bank account sort code set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = monthlyAccountingPeriod,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(bankAccount)
        )
        val builder = SummaryBusinessBankDetailsSectionBuilder(vatFinancials = Some(financials))
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "bankDetails.companyBankAccount.sortCode",
            "12-34-56",
            Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
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
          bankAccount = Some(VatBankAccount("BankAccountName", "00-00-00", "12345678"))
        )
        val builder = SummaryBusinessBankDetailsSectionBuilder(vatFinancials = Some(financials))
        builder.section.id mustBe "bankDetails"
        builder.section.rows.length mustEqual 4
      }
    }

  }
}