/*
 * Copyright 2020 HM Revenue & Customs
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

import models.api.Threshold
import models.view.{SummaryRow, SummarySection}
import models.{Returns, TradingDetails}
import uk.gov.hmrc.play.config.ServicesConfig

case class SummaryVatDetailsSectionBuilder(tradingDetails: Option[TradingDetails] = None,
                                           threshold: Option[Threshold],
                                           returnsBlock: Option[Returns]
                                          ) extends SummarySectionBuilder with ServicesConfig {

  override val sectionId: String = "vatDetails"
  private val thresholdBlock = threshold.getOrElse(throw new IllegalStateException("Missing threshold block to show summary"))
  private val voluntaryRegistration = !thresholdBlock.mandatoryRegistration

  def startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    returnsBlock.flatMap(_.start).flatMap(_.date) match {
      case Some(date) => date.format(presentationFormatter)
      case _ => s"pages.summary.$sectionId.mandatoryStartDate"
    },
    if (voluntaryRegistration) Some(controllers.routes.ReturnsController.voluntaryStartPage()) else None
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    tradingDetails.flatMap(_.tradingNameView).flatMap(_.tradingName).getOrElse("app.common.no"),
    Some(controllers.routes.TradingDetailsController.tradingNamePage())
  )

  val section: SummarySection =
    SummarySection(
      sectionId,
      rows = Seq((startDateRow, true), (tradingNameRow, true))
    )
}