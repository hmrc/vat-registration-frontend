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

import models.api._
import models.view.{SummaryRow, SummarySection}
import models.{AnnualCostsInclusiveView, AnnualCostsLimitedView, FrsStartDateView}
import org.apache.commons.lang3.StringUtils

case class SummaryFrsSectionBuilder(vatFrs: Option[VatFlatRateScheme] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "frs"

  private val decimalFormat = new DecimalFormat("#0.##")

  val joinFrsRow: SummaryRow = yesNoRow(
    "joinFrs",
    vatFrs.map(_.joinFrs),
    controllers.routes.FlatRateController.joinFrsPage()
  )

  val costsInclusiveRow: SummaryRow = SummaryRow(
    s"$sectionId.costsInclusive",
    vatFrs.flatMap(_.annualCostsInclusive).collect {
      case AnnualCostsInclusiveView.YES => "app.common.yes"
      case AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS => "pages.summary.frs.yes12Months"
      case AnnualCostsInclusiveView.NO => "pages.summary.frs.no"
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.annualCostsInclusivePage())
  )

  val costsLimitedRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    vatFrs.flatMap(_.annualCostsLimited).collect {
      case AnnualCostsLimitedView.YES => "app.common.yes"
      case AnnualCostsLimitedView.YES_WITHIN_12_MONTHS => "pages.summary.frs.yes12Months"
      case AnnualCostsLimitedView.NO => "pages.summary.frs.no"
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.annualCostsLimitedPage())
  )

  val useThisRateRow: SummaryRow = yesNoRow(
    "registerForFrs",
    vatFrs.flatMap(_.doYouWantToUseThisRate),
    vatFrs.flatMap(_.categoryOfBusiness) match {
      case Some("") | None => controllers.routes.FlatRateController.registerForFrsPage()
      case Some(_) => controllers.routes.FlatRateController.confirmSectorFrsPage()
    }
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    vatFrs.flatMap(_.whenDoYouWantToJoinFrs).flatMap {
      case FrsStartDateView.VAT_REGISTRATION_DATE => Some("pages.summary.frs.startDate.dateOfRegistration")
      case FrsStartDateView.DIFFERENT_DATE => vatFrs.flatMap(_.startDate).map(d => d.format(presentationFormatter))
      case _ => None
    }.getOrElse(""),
    Some(controllers.routes.FlatRateController.frsStartDatePage())
  )

  val flatRatePercentageRow: SummaryRow = SummaryRow(
    s"$sectionId.flatRate",
    vatFrs.flatMap(_.percentage).map(p => decimalFormat.format(p)).getOrElse(""),
    vatFrs.flatMap(_.categoryOfBusiness).collect{
    case s if StringUtils.isNotBlank(s) => controllers.routes.FlatRateController.confirmSectorFrsPage() }
  )

  val businessSectorRow: SummaryRow = SummaryRow(
    s"$sectionId.businessSector",
    vatFrs.flatMap(_.categoryOfBusiness).getOrElse(""),
    Some(controllers.routes.FlatRateController.confirmSectorFrsPage())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (joinFrsRow, vatFrs.map(_.joinFrs).isDefined),
      (costsInclusiveRow, vatFrs.flatMap(_.annualCostsInclusive).isDefined),
      (costsLimitedRow, vatFrs.flatMap(_.annualCostsLimited).isDefined),
      (businessSectorRow, vatFrs.flatMap(_.categoryOfBusiness).exists(StringUtils.isNotBlank)),
      (flatRatePercentageRow, vatFrs.flatMap(_.percentage).isDefined),
      (useThisRateRow, vatFrs.flatMap(_.doYouWantToUseThisRate).isDefined),
      (startDateRow, vatFrs.flatMap(_.whenDoYouWantToJoinFrs).isDefined)
    ),
    vatFrs.isDefined
  )

}
