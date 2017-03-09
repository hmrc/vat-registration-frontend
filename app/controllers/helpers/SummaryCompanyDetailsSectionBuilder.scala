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

package controllers.helpers

import javax.inject.Inject

import controllers.CommonPlayDependencies
import models.api.{VatFinancials, VatTradingDetails}
import models.view.{SummaryRow, SummarySection, VatReturnFrequency}
import play.api.UnexpectedException

class SummaryCompanyDetailsSectionBuilder(vatTradingDetails: VatTradingDetails, vatFinancials: VatFinancials)
                                         (implicit ds: CommonPlayDependencies)
  extends SummarySectionBuilder(ds) {

  def tradingNameRow: SummaryRow = SummaryRow(
    "companyDetails.tradingName",
    vatTradingDetails.tradingName match {
      case "" => Right(messagesApi("app.common.no"))
      case _ => Right(vatTradingDetails.tradingName)
    },
    Some(controllers.userJourney.routes.TradingNameController.show())
  )

  def estimatedSalesValueRow: SummaryRow = SummaryRow(
    "companyDetails.estimatedSalesValue",
    Right(s"£${vatFinancials.turnoverEstimate.toString}"),
    Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
  )

  def zeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSales",
    vatFinancials.zeroRatedSalesEstimate match {
      case Some(_) => Right(messagesApi("app.common.yes"))
      case None => Right(messagesApi("app.common.no"))
    },
    Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
  )

  def estimatedZeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSalesValue",
    Right(s"£${vatFinancials.zeroRatedSalesEstimate.getOrElse("").toString}"),
    Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
  )

  def vatChargeExpectancyRow: SummaryRow = SummaryRow(
    "companyDetails.reclaimMoreVat",
    if (vatFinancials.reclaimVatOnMostReturns) {
      Right(messagesApi("pages.summary.companyDetails.reclaimMoreVat.yes"))
    } else {
      Right(messagesApi("pages.summary.companyDetails.reclaimMoreVat.no"))
    },
    Some(controllers.userJourney.routes.VatChargeExpectancyController.show())
  )

  def accountingPeriodRow: SummaryRow = SummaryRow(
    "companyDetails.accountingPeriod",
    vatFinancials.vatAccountingPeriod.frequency match {
      case VatReturnFrequency.MONTHLY => Right(messagesApi("pages.summary.companyDetails.accountingPeriod.monthly"))
      case VatReturnFrequency.QUARTERLY => vatFinancials.vatAccountingPeriod.periodStart match {
        case Some(period) => Right(messagesApi(s"pages.summary.companyDetails.accountingPeriod.${period.substring(0, 3)}"))
        case None => throw UnexpectedException(Some(s"selected quarterly accounting period, but periodStart was None"))
      }
    },
    Some(controllers.userJourney.routes.VatReturnFrequencyController.show())
  )

  def companyBankAccountRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount",
    vatFinancials.bankAccount match {
      case Some(_) => Right(messagesApi("app.common.yes"))
      case None => Right(messagesApi("app.common.no"))
    },
    Some(controllers.userJourney.routes.CompanyBankAccountController.show())
  )

  def companyBankAccountNameRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.name",
    vatFinancials.bankAccount match {
      case Some(account) => Right(account.accountName)
      case None => Right(messagesApi("app.common.no"))
    },
    Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
  )

  def companyBankAccountNumberRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.number",
    vatFinancials.bankAccount match {
      case Some(account) => Right("****" + account.accountNumber.substring(4))
      case None => Right(messagesApi("app.common.no"))
    },
    Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
  )

  def companyBankAccountSortCodeRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.sortCode",
    vatFinancials.bankAccount match {
      case Some(account) => Right(account.accountSortCode)
      case None => Right(messagesApi("app.common.no"))
    },
    Some(controllers.userJourney.routes.CompanyBankAccountDetailsController.show())
  )

  def summarySection: SummarySection = SummarySection(
      id = "companyDetails",
      Seq(
        (tradingNameRow, true),
        (companyBankAccountRow, true),
        (companyBankAccountNameRow, vatFinancials.bankAccount.isDefined),
        (companyBankAccountNumberRow, vatFinancials.bankAccount.isDefined),
        (companyBankAccountSortCodeRow, vatFinancials.bankAccount.isDefined),
        (estimatedSalesValueRow, true),
        (zeroRatedSalesRow, true),
        (estimatedZeroRatedSalesRow, vatFinancials.zeroRatedSalesEstimate.isDefined),
        (vatChargeExpectancyRow, true),
        (accountingPeriodRow, true)
      )
    )
}
