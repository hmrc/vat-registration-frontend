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

import common.StringMasking._
import models.api._
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryCompanyDetailsSectionBuilder
(
  vatFinancials: Option[VatFinancials] = None,
  vatSicAndCompliance: Option[VatSicAndCompliance] = None,
  vatTradingDetails: Option[VatTradingDetails] = None
)
  extends SummarySectionBuilder {

  val estimatedSalesValueRow: SummaryRow = SummaryRow(
    "companyDetails.estimatedSalesValue",
    s"£${vatFinancials.map(_.turnoverEstimate.toString).getOrElse("0")}",
    Some(controllers.vatFinancials.routes.EstimateVatTurnoverController.show())
  )

  val zeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSales",
    vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
  )

  val estimatedZeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSalesValue",
    s"£${vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("")(_.toString)}",
    Some(controllers.vatFinancials.routes.EstimateZeroRatedSalesController.show())
  )

  val vatChargeExpectancyRow: SummaryRow = SummaryRow(
    "companyDetails.reclaimMoreVat",
    vatFinancials.filter(_.reclaimVatOnMostReturns).fold("pages.summary.companyDetails.reclaimMoreVat.no") {
      _ => "pages.summary.companyDetails.reclaimMoreVat.yes"
    },
    Some(controllers.vatFinancials.routes.VatChargeExpectancyController.show())
  )

  val accountingPeriodRow: SummaryRow = SummaryRow(
    "companyDetails.accountingPeriod",
    vatFinancials.map(_.accountingPeriods).collect {
      case VatAccountingPeriod(VatReturnFrequency.MONTHLY, _) => "pages.summary.companyDetails.accountingPeriod.monthly"
      case VatAccountingPeriod(VatReturnFrequency.QUARTERLY, Some(period)) =>
        s"pages.summary.companyDetails.accountingPeriod.${period.substring(0, 3)}"
    }.getOrElse(""),
    Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
  )

  val companyBankAccountRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
  )

  val companyBankAccountNameRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.name",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountName),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )

  val companyBankAccountNumberRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.number",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountNumber.mask(4)),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )

  val companyBankAccountSortCodeRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.sortCode",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountSortCode),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    "companyDetails.businessActivity.description",
    vatSicAndCompliance.collect {
      case VatSicAndCompliance(description, _, _) if StringUtils.isNotBlank(description) => description
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
  )

  val euGoodsRow: SummaryRow = SummaryRow(
    "companyDetails.eori.euGoods",
    vatTradingDetails.map(_.euTrading.selection).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())
  )

  val applyEoriRow: SummaryRow = SummaryRow(
    "companyDetails.eori",
    vatTradingDetails.flatMap(_.euTrading.eoriApplication).collect {
      case true => "app.common.applied"
    }.getOrElse("app.common.not.applied"),
    Some(controllers.vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show())
  )


  val section: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      (companyBusinessDescriptionRow, true),
      (companyBankAccountRow, true),
      (companyBankAccountNameRow, vatFinancials.flatMap(_.bankAccount).isDefined),
      (companyBankAccountNumberRow, vatFinancials.flatMap(_.bankAccount).isDefined),
      (companyBankAccountSortCodeRow, vatFinancials.flatMap(_.bankAccount).isDefined),
      (estimatedSalesValueRow, true),
      (zeroRatedSalesRow, true),
      (estimatedZeroRatedSalesRow, vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).isDefined),
      (vatChargeExpectancyRow, true),
      (accountingPeriodRow, true),
      (euGoodsRow, true),
      (applyEoriRow, vatTradingDetails.exists(_.euTrading.selection))
    )
  )
}
