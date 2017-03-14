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

import helpers.VatRegSpec
import models.api.{SicAndCompliance, VatAccountingPeriod, VatBankAccount, VatFinancials}
import models.view.{SummaryRow, VatReturnFrequency}
import play.api.UnexpectedException

class SummaryCompanyDetailsSectionBuilderSpec extends VatRegSpec {

  "The section builder composing a company details section" should {

    "with estimatedSalesValueRow render" should {

      "a £0 value should be returned as an estimated sales with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "companyDetails.estimatedSalesValue",
            "£0",
            Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
          )
      }

      "a real value should be returned as an estimated sales with vat financials containing a turnover estimate" in {
        val financials = VatFinancials(
          turnoverEstimate = 15000000L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "companyDetails.estimatedSalesValue",
            "£15000000",
            Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
          )
      }
    }

    "with zeroRatedSalesRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSales",
            "app.common.no",
            Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
          )
      }

      "a 'Yes' value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = Some(10000L)
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSales",
            "app.common.yes",
            Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
          )
      }
    }

    "with estimatedZeroRatedSalesRow render" should {

      "an empty value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSalesValue",
            "£",
            Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
          )
      }

      "a real value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = Some(10000L)
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSalesValue",
            "£10000",
            Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
          )
      }
    }

    "with vatChargeExpectancyRow render" should {

      "a 'No' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "companyDetails.reclaimMoreVat",
            "pages.summary.companyDetails.reclaimMoreVat.no",
            Some(controllers.userJourney.routes.VatChargeExpectancyController.show())
          )
      }

      "a 'Yes' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "companyDetails.reclaimMoreVat",
            "pages.summary.companyDetails.reclaimMoreVat.yes",
            Some(controllers.userJourney.routes.VatChargeExpectancyController.show())
          )
      }
    }

    "with accountingPeriodRow render" should {

      "a 'Once a month' answer should be returned when vat financials has an accounting period frequency set to monthly" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod(None, VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.monthly",
            Some(controllers.userJourney.routes.AccountingPeriodController.show())
          )
      }

      "a 'January, April, July and October' answer should be returned when accounting period frequency is set to quarterly with January as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod(Some("jan_apr_jul_oct"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.jan",
            Some(controllers.userJourney.routes.AccountingPeriodController.show())
          )
      }

      "a 'February, May, August and November' answer should be returned when accounting period frequency is set to quarterly with February as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod(Some("feb_may_aug_nov"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.feb",
            Some(controllers.userJourney.routes.AccountingPeriodController.show())
          )
      }

      "a 'March, June, September and December' answer should be returned when accounting period frequency is set to quarterly with March as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod(Some("mar_jun_sep_dec"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.mar",
            Some(controllers.userJourney.routes.AccountingPeriodController.show())
          )
      }

      "an exception should be thrown when accounting period frequency is set to quarterly with no accounting period set" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod(None, VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedSalesEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        assertThrows[UnexpectedException] {
          builder.accountingPeriodRow
        }
      }
    }

    "with companyBankAccountRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount",
            "app.common.no",
            Some(controllers.userJourney.routes.CompanyBankAccountController.show())
          )
      }

      "a 'Yes' value should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = None,
          bankAccount = Some(VatBankAccount(accountNumber = "12345678"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount",
            "app.common.yes",
            Some(controllers.userJourney.routes.CompanyBankAccountController.show())
          )
      }
    }

    "with companyBankAccountNameRow render" should {

      "a 'No' value should be returned with an empty bank account name in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.name",
            "app.common.no",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real name value should be returned with bank account name set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = None,
          bankAccount = Some(VatBankAccount(accountName = "John Smith"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.name",
            "John Smith",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountNumberRow render" should {

      "a 'No' value should be returned with an empty bank account number in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.number",
            "app.common.no",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real bank account number's last 4 digits the rest being masked, should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = None,
          bankAccount = Some(VatBankAccount(accountNumber = "12345678"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.number",
            "****5678",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountSortCodeRow render" should {

      "a 'No' value should be returned with an empty bank account sort code in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.sortCode",
            "app.common.no",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real sort code value should be returned with bank account sort code set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          vatAccountingPeriod = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedSalesEstimate = None,
          bankAccount = Some(VatBankAccount(accountSortCode = "01-23-45"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, SicAndCompliance())
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.sortCode",
            "01-23-45",
            Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBusinessDescriptionRow render" should {

      "a 'No' value should be returned with an empty description in sic and compliance" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyDetails.businessActivity.description",
            "app.common.no",
            Some(controllers.userJourney.routes.BusinessActivityDescriptionController.show())
          )
      }

      "a real sort code value should be returned with bank account sort code set in vat financials" in {
        val compliance = SicAndCompliance("Business Described")
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, compliance)
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyDetails.businessActivity.description",
            "Business Described",
            Some(controllers.userJourney.routes.BusinessActivityDescriptionController.show())
          )
      }
    }

  }
}