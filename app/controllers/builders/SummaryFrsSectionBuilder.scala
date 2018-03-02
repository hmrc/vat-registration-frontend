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

import features.returns.Start
import frs.{AnnualCosts, FlatRateScheme}
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryFrsSectionBuilder(vatFrs: Option[FlatRateScheme] = None) extends SummarySectionBuilder {
  override val sectionId: String = "frs"

  private val decimalFormat = new DecimalFormat("#0.##")

  val joinFrsRow: SummaryRow = yesNoRow(
    "joinFrs",
    vatFrs.flatMap(_.joinFrs),
    controllers.routes.FlatRateController.joinFrsPage()
  )

  val costsInclusiveRow: SummaryRow = SummaryRow(
    s"$sectionId.costsInclusive",
    vatFrs.flatMap(_.overBusinessGoods).collect {
      case AnnualCosts.AlreadyDoesSpend => "app.common.yes"
      case AnnualCosts.WillSpend => "pages.summary.frs.yes12Months"
      case AnnualCosts.DoesNotSpend => "pages.summary.frs.no"
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.annualCostsInclusivePage())
  )

  val costsLimitedRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    vatFrs.flatMap(_.overBusinessGoodsPercent).collect {
      case AnnualCosts.AlreadyDoesSpend => "app.common.yes"
      case AnnualCosts.WillSpend => "pages.summary.frs.yes12Months"
      case AnnualCosts.DoesNotSpend => "pages.summary.frs.no"
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.annualCostsLimitedPage())
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    vatFrs.flatMap(_.frsStart).flatMap {
      case Start(None) => Some("pages.summary.frs.startDate.dateOfRegistration")
      case Start(frds) => frds.map(d => d.format(presentationFormatter))
      case _ => None
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.frsStartDatePage())
  )

  val flatRatePercentageRow: SummaryRow = SummaryRow(
    s"$sectionId.flatRate",
    vatFrs.flatMap(_.percent).map(p => decimalFormat.format(p)).getOrElse(""),
    vatFrs.flatMap(_.categoryOfBusiness).collect{
    case s if StringUtils.isNotBlank(s) => controllers.routes.FlatRateController.confirmSectorFrsPage() }
  )

  val businessSectorRow: SummaryRow = SummaryRow(
    s"$sectionId.businessSector",
    vatFrs.flatMap(_.categoryOfBusiness).getOrElse(""),
    Some(controllers.routes.FlatRateController.confirmSectorFrsPage())
  )

  val section: SummarySection = {
    SummarySection(
    sectionId,
    Seq(
      (joinFrsRow, vatFrs.flatMap(_.joinFrs).isDefined),
      (costsInclusiveRow, vatFrs.flatMap(_.overBusinessGoods).isDefined),
      (costsLimitedRow, vatFrs.flatMap(_.overBusinessGoodsPercent).isDefined),
      (businessSectorRow, vatFrs.flatMap(_.categoryOfBusiness).exists(StringUtils.isNotBlank)),
      (flatRatePercentageRow, vatFrs.flatMap(_.percent).isDefined),
      (startDateRow, vatFrs.flatMap(_.frsStart).isDefined)
    ),
    vatFrs.isDefined
  )}

}
