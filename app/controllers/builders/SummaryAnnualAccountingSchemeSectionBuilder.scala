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

import features.returns.{Frequency, Returns}
import models.view.{SummaryRow, SummarySection}

case class SummaryAnnualAccountingSchemeSectionBuilder(returns: Option[Returns])
  extends SummarySectionBuilder {

  override val sectionId: String = "annualAccountingScheme"

  val vatChargeExpectancyRow: SummaryRow = SummaryRow(
    s"$sectionId.reclaimMoreVat",
    returns.flatMap(_.reclaimVatOnMostReturns).fold("") {
      reclaim => s"pages.summary.$sectionId.reclaimMoreVat.${if (reclaim) "yes" else "no"}"
    },
    Some(features.returns.routes.ReturnsController.chargeExpectancyPage())
  )

  val accountingPeriodRow: SummaryRow = SummaryRow(
    s"$sectionId.accountingPeriod",
    (returns.flatMap(_.frequency), returns.flatMap(_.staggerStart)) match {
      case (Some(Frequency.monthly), _) => s"pages.summary.$sectionId.accountingPeriod.monthly"
      case (Some(Frequency.quarterly), Some(period)) =>
        s"pages.summary.$sectionId.accountingPeriod.${period.substring(0, 3)}"
      case _ => ""
    },
    Some(features.returns.routes.ReturnsController.accountPeriodsPage())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (vatChargeExpectancyRow, true),
      (accountingPeriodRow, true)
    )
  )
}
