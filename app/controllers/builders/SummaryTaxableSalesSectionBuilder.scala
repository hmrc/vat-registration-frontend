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

case class SummaryTaxableSalesSectionBuilder
(
  vatFinancials: Option[VatFinancials] = None
)
  extends SummarySectionBuilder {

  val estimatedSalesValueRow: SummaryRow = SummaryRow(
    "taxableSales.estimatedSalesValue",
    s"£${vatFinancials.map(_.turnoverEstimate.toString).getOrElse("0")}",
    Some(controllers.vatFinancials.routes.EstimateVatTurnoverController.show())
  )

  val zeroRatedSalesRow: SummaryRow = SummaryRow(
    "taxableSales.zeroRatedSales",
    vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
  )

  val estimatedZeroRatedSalesRow: SummaryRow = SummaryRow(
    "taxableSales.zeroRatedSalesValue",
    s"£${vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).fold("")(_.toString)}",
    Some(controllers.vatFinancials.routes.EstimateZeroRatedSalesController.show())
  )

  val section: SummarySection = SummarySection(
    id = "taxableSales",
    Seq(
      (estimatedSalesValueRow, true),
      (zeroRatedSalesRow, true),
      (estimatedZeroRatedSalesRow, vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).isDefined)
    )
  )
}
