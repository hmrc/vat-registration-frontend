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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import features.returns.models.Returns
import features.tradingDetails.TradingDetails
import models.MonthYearModel
import models.api.Threshold
import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig



case class SummaryVatDetailsSectionBuilder (tradingDetails: Option[TradingDetails] = None,
                                            threshold: Option[Threshold],
                                            returnsBlock : Option[Returns],
                                            incorpDate: Option[LocalDate] = None,
                                            taxableThreshold: String
                                           ) extends SummarySectionBuilder with ServicesConfig {
  override val sectionId: String      = "vatDetails"
  val monthYearPresentationFormatter  = DateTimeFormatter.ofPattern("MMMM y")
  val serviceName                     = "vat-registration-eligibility-frontend"

  private val thresholdBlock        = threshold.getOrElse(throw new IllegalStateException("Missing threshold block to show summary"))
  private val voluntaryRegistration = !thresholdBlock.mandatoryRegistration

  val overThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdSelection",
    thresholdBlock.overThresholdOccuredTwelveMonth.fold("app.common.no")(_ => "app.common.yes"),
    Some(getUrl(serviceName,"gone-over-threshold")),
    Seq(incorpDate.fold("")(_.format(MonthYearModel.FORMAT_DD_MMMM_Y)), taxableThreshold)
  )

  val overThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdDate",
    thresholdBlock.overThresholdOccuredTwelveMonth.map(_.format(monthYearPresentationFormatter)).getOrElse(""),
    Some(getUrl(serviceName,"gone-over-threshold")),
    Seq(taxableThreshold)
  )

  val dayMonthYearPresentationFormatter = DateTimeFormatter.ofPattern("dd MMMM y")

  val pastOverThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.expectationOverThresholdSelection",
    thresholdBlock.pastOverThresholdDateThirtyDays.fold("app.common.no")(_ => "app.common.yes"),
    Some(getUrl(serviceName,"gone-over-threshold-period"))
  )

  val pastOverThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.expectationOverThresholdDate",
    thresholdBlock.pastOverThresholdDateThirtyDays.map(_.format(dayMonthYearPresentationFormatter)).getOrElse(""),
    Some(getUrl(serviceName,"gone-over-threshold-period"))
  )

  val overThresholdThirtySelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdThirtySelection",
    thresholdBlock.overThresholdDateThirtyDays.fold(
      if(thresholdBlock.mandatoryRegistration && incorpDate.isEmpty) "app.common.yes" else "app.common.no"
    )(_ => "app.common.yes"),
    Some(getUrl(serviceName,"make-more-taxable-sales")),
    Seq(taxableThreshold)
  )

  val necessityRow: SummaryRow = SummaryRow(
    s"$sectionId.necessity",
    s"app.common.${if (voluntaryRegistration) "yes" else "no"}",
    if (voluntaryRegistration) {
      Some(getUrl(serviceName, "register-voluntary"))
    } else {
      None
    }
  )

  val voluntaryReasonRow: SummaryRow = SummaryRow(
    s"$sectionId.voluntaryRegistrationReason",
    thresholdBlock.voluntaryReason.fold("app.common.no"){
      case Threshold.SELLS => "pages.summary.voluntaryReason.sells"
      case Threshold.INTENDS_TO_SELL => "pages.summary.voluntaryReason.intendsToSell"
    },
    Some(getUrl(serviceName,"registration-reason"))
  )

  def startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    returnsBlock.flatMap(_.start).flatMap(_.date) match {
      case Some(date) => date.format(presentationFormatter)
      case _ => s"pages.summary.$sectionId.mandatoryStartDate"
    },
    if (voluntaryRegistration) Some(features.returns.controllers.routes.ReturnsController.voluntaryStartPage()) else incorpDate match {
      case Some(_) => Some(features.returns.controllers.routes.ReturnsController.mandatoryStartPage())
      case None => None
    }
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    tradingDetails.flatMap(_.tradingNameView).flatMap(_.tradingName).getOrElse("app.common.no"),
    Some(controllers.routes.TradingDetailsController.tradingNamePage())
  )

  val section: SummarySection =
    SummarySection(
      sectionId,
      rows = Seq(
        (overThresholdThirtySelectionRow, true),
        (pastOverThresholdSelectionRow, incorpDate.isDefined ),
        (pastOverThresholdDateRow, incorpDate.isDefined && thresholdBlock.pastOverThresholdDateThirtyDays.isDefined),
        (overThresholdSelectionRow, incorpDate.isDefined),
        (overThresholdDateRow, incorpDate.isDefined && thresholdBlock.overThresholdOccuredTwelveMonth.isDefined),
        (necessityRow, voluntaryRegistration),
        (voluntaryReasonRow, voluntaryRegistration),
        (startDateRow, true),
        (tradingNameRow, true)
      )
    )

  def getUrl(serviceName: String, uri: String): Call = {
    val basePath = getConfString(s"$serviceName.www.host", "")
    val mainUri = getConfString(s"$serviceName.uri","/register-for-vat/")
    val serviceUri = getConfString(s"$serviceName.uris.$uri",uri)
    Call("Get", s"$basePath$mainUri$serviceUri")
  }
}
