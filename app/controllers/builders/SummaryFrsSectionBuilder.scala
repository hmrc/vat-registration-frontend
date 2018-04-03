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

import java.text.DecimalFormat

import features.returns.models.Start
import features.turnoverEstimates.TurnoverEstimates
import frs.FlatRateScheme
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryFrsSectionBuilder(vatFrs: Option[FlatRateScheme] = None,
                                    calculatedOnEstimatedSales: Option[Long],
                                    businessType: Option[String],
                                    turnoverEstimates:Option[TurnoverEstimates]) extends SummarySectionBuilder {
  override val sectionId: String = "frs"

  private val decimalFormat = new DecimalFormat("#0.##")

  val joinFrsRow: SummaryRow = yesNoRow(
    "joinFrs",
    vatFrs.flatMap(_.joinFrs),
    features.frs.controllers.routes.FlatRateController.joinFrsPage()
  )

  val costsInclusiveRow: SummaryRow = SummaryRow(
    s"$sectionId.costsInclusive",
    if(vatFrs.flatMap(_.overBusinessGoods).contains(true)) "app.common.yes" else "app.common.no",
    Some(features.frs.controllers.routes.FlatRateController.annualCostsInclusivePage())
  )

  val estimateTotalSalesRow: SummaryRow = SummaryRow(
    s"$sectionId.estimateTotalSales",
    s"£${vatFrs.flatMap(_.estimateTotalSales.map("%,d".format(_))).getOrElse("0")}",
    Some(features.frs.controllers.routes.FlatRateController.estimateTotalSales())
  )

  val costsLimitedRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    if(vatFrs.flatMap(_.overBusinessGoodsPercent).contains(true)) "app.common.yes" else "app.common.no",
    Some(features.frs.controllers.routes.FlatRateController.annualCostsLimitedPage()),
    Seq(calculatedOnEstimatedSales.map("%,d".format(_)).getOrElse("0"))
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    vatFrs.flatMap(_.frsStart).flatMap {
      case Start(None) => Some("pages.summary.frs.startDate.dateOfRegistration")
      case Start(frds) => frds.map(d => d.format(presentationFormatter))
      case _ => None
    }.getOrElse(""),
    Some(features.frs.controllers.routes.FlatRateController.frsStartDatePage())
  )

  val flatRatePercentageRow: SummaryRow = SummaryRow(
    s"$sectionId.flatRate",
    if(vatFrs.flatMap(_.useThisRate).contains(true)) "app.common.yes" else "app.common.no",
    vatFrs.flatMap(_.categoryOfBusiness).collect {
      case s if s.nonEmpty => features.frs.controllers.routes.FlatRateController.yourFlatRatePage()
      case _               => features.frs.controllers.routes.FlatRateController.registerForFrsPage()
    },
    Seq(vatFrs.flatMap(_.percent).getOrElse(0.0).toString)
  )

  val businessSectorRow: SummaryRow = SummaryRow(
    s"$sectionId.businessSector",
    businessType.getOrElse(""),
    Some(features.frs.controllers.routes.FlatRateController.businessType())
  )

  val joinFrsContainsTrue: Boolean  = vatFrs.flatMap(_.joinFrs).contains(true)
  val isflatRatePercentYes: Boolean = vatFrs.flatMap(_.useThisRate).contains(true)
  val isBusinessGoodsYes: Boolean   = joinFrsContainsTrue && vatFrs.flatMap(_.overBusinessGoods).contains(true)

  val section: SummarySection = {
    SummarySection(
    sectionId,
    Seq(
      (joinFrsRow, vatFrs.flatMap(_.joinFrs).isDefined),
      (costsInclusiveRow, joinFrsContainsTrue && vatFrs.flatMap(_.overBusinessGoods).isDefined),
      (estimateTotalSalesRow, isBusinessGoodsYes && vatFrs.flatMap(_.estimateTotalSales).isDefined),
      (costsLimitedRow, isBusinessGoodsYes && vatFrs.flatMap(_.overBusinessGoodsPercent).isDefined),
      (businessSectorRow, joinFrsContainsTrue && vatFrs.flatMap(_.categoryOfBusiness).exists(StringUtils.isNotBlank)),
      (flatRatePercentageRow, joinFrsContainsTrue && vatFrs.flatMap(_.useThisRate).isDefined),
      (startDateRow, isflatRatePercentYes && vatFrs.flatMap(_.frsStart).isDefined)
    ),
      turnoverEstimates.exists(toe => if(toe.vatTaxable > 150000L) false else vatFrs.isDefined)
  )}

}
