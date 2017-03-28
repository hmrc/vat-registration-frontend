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
import models.api._
import models.view.SummaryRow
import models.view.vatFinancials.VatReturnFrequency
import play.api.UnexpectedException

class SummaryCompanyDetailsSectionBuilderSpec extends VatRegSpec {

  "The section builder composing a company details section" should {

    "with estimatedSalesValueRow render" should {

      "a £0 value should be returned as an estimated sales with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "companyDetails.estimatedSalesValue",
            "£0",
            Some(controllers.userJourney.vatFinancials.routes.EstimateVatTurnoverController.show())
          )
      }

      "a real value should be returned as an estimated sales with vat financials containing a turnover estimate" in {
        val financials = VatFinancials(
          turnoverEstimate = 15000000L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "companyDetails.estimatedSalesValue",
            "£15000000",
            Some(controllers.userJourney.vatFinancials.routes.EstimateVatTurnoverController.show())
          )
      }
    }

    "with zeroRatedSalesRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSales",
            "app.common.no",
            Some(controllers.userJourney.vatFinancials.routes.ZeroRatedSalesController.show())
          )
      }

      "a 'Yes' value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = Some(10000L)
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.zeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSales",
            "app.common.yes",
            Some(controllers.userJourney.vatFinancials.routes.ZeroRatedSalesController.show())
          )
      }
    }

    "with estimatedZeroRatedSalesRow render" should {

      "an empty value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSalesValue",
            "£",
            Some(controllers.userJourney.vatFinancials.routes.EstimateZeroRatedSalesController.show())
          )
      }

      "a real value should be returned with a zero rated sales estimate in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = Some(10000L)
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.estimatedZeroRatedSalesRow mustBe
          SummaryRow(
            "companyDetails.zeroRatedSalesValue",
            "£10000",
            Some(controllers.userJourney.vatFinancials.routes.EstimateZeroRatedSalesController.show())
          )
      }
    }

    "with vatChargeExpectancyRow render" should {

      "a 'No' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "companyDetails.reclaimMoreVat",
            "pages.summary.companyDetails.reclaimMoreVat.no",
            Some(controllers.userJourney.vatFinancials.routes.VatChargeExpectancyController.show())
          )
      }

      "a 'Yes' should be returned when vat financials has a positive to reclaiming VAT on more return" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.vatChargeExpectancyRow mustBe
          SummaryRow(
            "companyDetails.reclaimMoreVat",
            "pages.summary.companyDetails.reclaimMoreVat.yes",
            Some(controllers.userJourney.vatFinancials.routes.VatChargeExpectancyController.show())
          )
      }
    }

    "with accountingPeriodRow render" should {

      "a 'Once a month' answer should be returned when vat financials has an accounting period frequency set to monthly" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(None, VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.monthly",
            Some(controllers.userJourney.vatFinancials.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'January, April, July and October' answer should be returned when accounting period frequency is set to quarterly with January as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(Some("jan_apr_jul_oct"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.jan",
            Some(controllers.userJourney.vatFinancials.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'February, May, August and November' answer should be returned when accounting period frequency is set to quarterly with February as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(Some("feb_may_aug_nov"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.feb",
            Some(controllers.userJourney.vatFinancials.routes.VatReturnFrequencyController.show())
          )
      }

      "a 'March, June, September and December' answer should be returned when accounting period frequency is set to quarterly with March as a start" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(Some("mar_jun_sep_dec"), VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.accountingPeriodRow mustBe
          SummaryRow(
            "companyDetails.accountingPeriod",
            "pages.summary.companyDetails.accountingPeriod.mar",
            Some(controllers.userJourney.vatFinancials.routes.VatReturnFrequencyController.show())
          )
      }

      "an exception should be thrown when accounting period frequency isn't set" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        assertThrows[UnexpectedException](builder.accountingPeriodRow)
      }

      "an exception should be thrown when accounting period frequency is set to quarterly with no accounting period set" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(None, VatReturnFrequency.QUARTERLY),
          reclaimVatOnMostReturns = true,
          zeroRatedTurnoverEstimate = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        assertThrows[UnexpectedException](builder.accountingPeriodRow)
      }
    }

    "with companyBankAccountRow render" should {

      "a 'No' value should be returned with an empty vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount",
            "app.common.no",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show())
          )
      }

      "a 'Yes' value should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(VatBankAccount(accountNumber = "12345678"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.companyBankAccountRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount",
            "app.common.yes",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show())
          )
      }
    }

    "with companyBankAccountNameRow render" should {

      "a 'No' value should be returned with an empty bank account name in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.name",
            "app.common.no",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real name value should be returned with bank account name set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(VatBankAccount(accountName = "John Smith"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.companyBankAccountNameRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.name",
            "John Smith",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountNumberRow render" should {

      "a 'No' value should be returned with an empty bank account number in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.number",
            "app.common.no",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real bank account number's last 4 digits the rest being masked, should be returned with bank account number set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(VatBankAccount(accountNumber = "12345678"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.companyBankAccountNumberRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.number",
            "****5678",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBankAccountSortCodeRow render" should {

      "a 'No' value should be returned with an empty bank account sort code in vat financials" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.sortCode",
            "app.common.no",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }

      "a real sort code value should be returned with bank account sort code set in vat financials" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod.empty,
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = Some(VatBankAccount(accountSortCode = "01-23-45"))
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.companyBankAccountSortCodeRow mustBe
          SummaryRow(
            "companyDetails.companyBankAccount.sortCode",
            "01-23-45",
            Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
          )
      }
    }

    "with companyBusinessDescriptionRow render" should {

      "a 'No' value should be returned with an empty description in sic and compliance" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, VatSicAndCompliance.empty)
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyDetails.businessActivity.description",
            "app.common.no",
            Some(controllers.userJourney.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          )
      }

      "a real sort code value should be returned with bank account sort code set in vat financials" in {
        val compliance = VatSicAndCompliance("Business Described", None)
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, compliance)
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyDetails.businessActivity.description",
            "Business Described",
            Some(controllers.userJourney.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          )
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val financials = VatFinancials(
          turnoverEstimate = 0L,
          accountingPeriods = VatAccountingPeriod(None, VatReturnFrequency.MONTHLY),
          reclaimVatOnMostReturns = false,
          zeroRatedTurnoverEstimate = None,
          bankAccount = None
        )
        val builder = SummaryCompanyDetailsSectionBuilder(financials, VatSicAndCompliance.empty)
        builder.section.id mustBe "companyDetails"
        builder.section.rows.length mustEqual 10
      }
    }

  }
}