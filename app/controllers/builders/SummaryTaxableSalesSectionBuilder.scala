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

import features.turnoverEstimates.TurnoverEstimates
import models.view.{SummaryRow, SummarySection}

case class SummaryTaxableSalesSectionBuilder(turnoverEstimates: Option[TurnoverEstimates] = None) extends SummarySectionBuilder {
  override val sectionId: String = "taxableSales"

  val estimatedSalesValueRow: SummaryRow = SummaryRow(
    s"$sectionId.estimatedSalesValue",
    s"£${turnoverEstimates.map(_.vatTaxable.toString).getOrElse("0")}",
    Some(features.turnoverEstimates.routes.TurnoverEstimatesController.showEstimateVatTurnover())
  )


  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (estimatedSalesValueRow, true)
    )
  )
}
