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
import models.view.frs.{AnnualCostsInclusiveView, AnnualCostsLimitedView}
import models.view.{SummaryRow, SummarySection}

case class SummaryFrsSectionBuilder
(
  vatFrs: Option[VatFlatRateScheme] = None
)
  extends SummarySectionBuilder {

  override val sectionId: String = "frs"

  val joinFrsRow: SummaryRow = yesNoRow(
    "joinFrs",
    vatFrs.map(_.joinFrs),
    controllers.frs.routes.JoinFrsController.show()
  )

  val costsInclusiveRow: SummaryRow = SummaryRow(
    s"$sectionId.costsInclusive",
    vatFrs.flatMap(_.annualCostsInclusive).collect {
      case AnnualCostsInclusiveView.YES => "pages.summary.frs.costsInclusive.lessThan1k"
      case AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS => "pages.summary.frs.costsInclusive.futureLessThan1k"
      case AnnualCostsInclusiveView.NO => "pages.summary.frs.costsInclusive.moreThan1k"
    }.getOrElse(""),
    Some(controllers.frs.routes.AnnualCostsInclusiveController.show())
  )

  val costsLimimtedRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    vatFrs.flatMap(_.annualCostsLimited).collect {
      case AnnualCostsLimitedView.YES => "pages.summary.frs.costsLimited.lessThan2percent"
      case AnnualCostsLimitedView.YES_WITHIN_12_MONTHS => "pages.summary.frs.costsLimited.futureLessThan2percent"
      case AnnualCostsLimitedView.NO => "pages.summary.frs.costsLimited.moreThan2percent"
    }.getOrElse(""),
    Some(controllers.frs.routes.AnnualCostsLimitedController.show())
  )

  val useThisRateRow: SummaryRow = yesNoRow(
    "registerForFrs",
    vatFrs.flatMap(_.doYouWantToUseThisRate),
    controllers.frs.routes.RegisterForFrsController.show()
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    vatFrs.flatMap(_.whenDoYouWantToJoinFrs).getOrElse(""),
    Some(controllers.frs.routes.AnnualCostsInclusiveController.show()) //TODO fix once screen is done
  )


  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (joinFrsRow, vatFrs.map(_.joinFrs).isDefined),
      (costsInclusiveRow, vatFrs.flatMap(_.annualCostsInclusive).isDefined),
      (costsLimimtedRow, vatFrs.flatMap(_.annualCostsLimited).isDefined),
      (useThisRateRow, vatFrs.flatMap(_.doYouWantToUseThisRate).isDefined),
      (startDateRow, vatFrs.flatMap(_.whenDoYouWantToJoinFrs).isDefined)
    )
  )

}
