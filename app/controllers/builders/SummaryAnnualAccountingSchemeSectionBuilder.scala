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

import models.api._
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import models.view.{SummaryRow, SummarySection}

case class SummaryAnnualAccountingSchemeSectionBuilder(vatFinancials: Option[VatFinancials] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "annualAccountingScheme"

  val vatChargeExpectancyRow: SummaryRow = SummaryRow(
    s"$sectionId.reclaimMoreVat",
    vatFinancials.filter(_.reclaimVatOnMostReturns).fold(s"pages.summary.$sectionId.reclaimMoreVat.no") {
      _ => s"pages.summary.$sectionId.reclaimMoreVat.yes"
    },
    Some(controllers.vatFinancials.routes.VatChargeExpectancyController.show())
  )

  val accountingPeriodRow: SummaryRow = SummaryRow(
    s"$sectionId.accountingPeriod",
    vatFinancials.map(_.accountingPeriods).collect {
      case VatAccountingPeriod(VatReturnFrequency.MONTHLY, _) => s"pages.summary.$sectionId.accountingPeriod.monthly"
      case VatAccountingPeriod(VatReturnFrequency.QUARTERLY, Some(period)) =>
        s"pages.summary.$sectionId.accountingPeriod.${period.substring(0, 3)}"
    }.getOrElse(""),
    Some(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (vatChargeExpectancyRow, true),
      (accountingPeriodRow, true)
    )
  )
}
