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

import features.returns.Returns
import features.tradingDetails.TradingDetails
import models.api.Threshold
import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig



case class SummaryVatDetailsSectionBuilder (tradingDetails: Option[TradingDetails] = None,
                                            threshold: Option[Threshold],
                                            returnsBlock : Option[Returns],
                                            incorpDate: Option[LocalDate] = None)

  extends SummarySectionBuilder with ServicesConfig{

  override val sectionId: String = "vatDetails"
  val monthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")
  val serviceName = "vat-registration-eligibility-frontend"

  private val thresholdBlock = threshold.getOrElse(throw new IllegalStateException("Missing threshold block to show summary"))
  private val voluntaryRegistration = !thresholdBlock.mandatoryRegistration

  val taxableTurnoverRow: SummaryRow = SummaryRow(
    s"$sectionId.taxableTurnover",
    s"app.common.${if (voluntaryRegistration) "no" else "yes"}",
    Some(getUrl(serviceName,"sales-over-threshold"))
  )

  val overThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdSelection",
    thresholdBlock.overThresholdDate.fold("app.common.no")(_ => "app.common.yes"),
    Some(getUrl(serviceName,"turnover-over-threshold"))
  )

  val overThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdDate",
    thresholdBlock.overThresholdDate
      .map(_.format(monthYearPresentationFormatter))
      .getOrElse(""),
      Some(getUrl(serviceName,"turnover-over-threshold"))
  )

  val expectedOverThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.expectedOverThresholdSelection",
    thresholdBlock.expectedOverThresholdDate.fold("app.common.no")(_ => "app.common.yes"),
    Some(getUrl(serviceName,"thought-over-threshold"))
  )

  val expectedOverThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.expectedOverThresholdDate",
    thresholdBlock.expectedOverThresholdDate
      .map(_.format(presentationFormatter))
      .getOrElse(""),
    Some(getUrl(serviceName,"thought-over-threshold"))
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
    if (voluntaryRegistration) Some(features.returns.routes.ReturnsController.voluntaryStartPage()) else incorpDate match {
      case Some(_) => Some(features.returns.routes.ReturnsController.mandatoryStartPage())
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
        (taxableTurnoverRow, incorpDate.isEmpty),
        (overThresholdSelectionRow, incorpDate.isDefined),
        (overThresholdDateRow, incorpDate.isDefined && thresholdBlock.overThresholdDate.isDefined),
        (expectedOverThresholdSelectionRow, incorpDate.isDefined ),
        (expectedOverThresholdDateRow, incorpDate.isDefined && thresholdBlock.expectedOverThresholdDate.isDefined),
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
