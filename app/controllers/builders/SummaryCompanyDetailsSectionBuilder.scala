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
import models.api.{VatAccountingPeriod, VatFinancials, VatSicAndCompliance}
import models.view.vatFinancials.VatReturnFrequency
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryCompanyDetailsSectionBuilder
(
  vatFinancials: Option[VatFinancials] = None,
  vatSicAndCompliance: Option[VatSicAndCompliance] = None
)
  extends SummarySectionBuilder {

  def estimatedSalesValueRow: SummaryRow = SummaryRow(
    "companyDetails.estimatedSalesValue",
    s"£${vatFinancials.map(_.turnoverEstimate.toString).getOrElse("0")}",
    Some(controllers.userJourney.vatFinancials.routes.EstimateVatTurnoverController.show())
  )

  def zeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSales",
    vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.userJourney.vatFinancials.routes.ZeroRatedSalesController.show())
  )

  def estimatedZeroRatedSalesRow: SummaryRow = SummaryRow(
    "companyDetails.zeroRatedSalesValue",
    s"£${vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("")(_.toString)}",
    Some(controllers.userJourney.vatFinancials.routes.EstimateZeroRatedSalesController.show())
  )

  def vatChargeExpectancyRow: SummaryRow = SummaryRow(
    "companyDetails.reclaimMoreVat",
    vatFinancials.filter(_.reclaimVatOnMostReturns).fold("pages.summary.companyDetails.reclaimMoreVat.no") {
      _ => "pages.summary.companyDetails.reclaimMoreVat.yes"
    },
    Some(controllers.userJourney.vatFinancials.routes.VatChargeExpectancyController.show())
  )

  def accountingPeriodRow: SummaryRow = SummaryRow(
    "companyDetails.accountingPeriod",
    vatFinancials.map(_.accountingPeriods).collect {
      case VatAccountingPeriod(VatReturnFrequency.MONTHLY, _) => "pages.summary.companyDetails.accountingPeriod.monthly"
      case VatAccountingPeriod(VatReturnFrequency.QUARTERLY, Some(period)) =>
        s"pages.summary.companyDetails.accountingPeriod.${period.substring(0, 3)}"
    }.getOrElse(""),
    Some(controllers.userJourney.vatFinancials.routes.VatReturnFrequencyController.show())
  )

  def companyBankAccountRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show())
  )

  def companyBankAccountNameRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.name",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountName),
    Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
  )

  def companyBankAccountNumberRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.number",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountNumber.mask(4)),
    Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
  )

  def companyBankAccountSortCodeRow: SummaryRow = SummaryRow(
    "companyDetails.companyBankAccount.sortCode",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountSortCode),
    Some(controllers.userJourney.vatFinancials.routes.CompanyBankAccountDetailsController.show())
  )

  def companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    "companyDetails.businessActivity.description",
    vatSicAndCompliance.collect {
      case VatSicAndCompliance(description, _) if StringUtils.isNotBlank(description) => description
    }.getOrElse("app.common.no"),
    Some(controllers.userJourney.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
  )


  def section: SummarySection = SummarySection(
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
      (accountingPeriodRow, true)
    )
  )
}
