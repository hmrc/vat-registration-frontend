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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import models.api.VatChoice.NECESSITY_VOLUNTARY
import models.api.{VatChoice, VatStartDate, VatTradingDetails}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.view.{SummaryRow, SummarySection}

case class SummaryVatDetailsSectionBuilder(vatTradingDetails: Option[VatTradingDetails] = None,
                                           dateOfIncorporation: LocalDate)
  extends SummarySectionBuilder {

  override val sectionId: String = "vatDetails"
  val monthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")

  private val voluntaryRegistration = vatTradingDetails.exists(_.registeringVoluntarily)

  val taxableTurnoverRow: SummaryRow = SummaryRow(
    s"$sectionId.taxableTurnover",
    s"app.common.${if (voluntaryRegistration) "no" else "yes"}",
    Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
  )

  val overThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdSelection",
    vatTradingDetails.flatMap(_.vatChoice.vatThresholdPostIncorp.map(_.overThresholdSelection)).collect {
      case true => "app.common.yes"
      case false => "app.common.no"
    }.getOrElse(""),
    Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show()),
    questionArg = Some(dateOfIncorporation.format(presentationFormatter))
  )

  val overThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdDate",
    vatTradingDetails.flatMap(_.vatChoice.vatThresholdPostIncorp.map(_.overThresholdDate)).collect {
      case Some(date) => date.format(monthYearPresentationFormatter)
    }.getOrElse(""),
    Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show())
  )

  val necessityRow: SummaryRow = SummaryRow(
    s"$sectionId.necessity",
    s"app.common.${if (voluntaryRegistration) "yes" else "no"}",
    if (voluntaryRegistration) Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show()) else None
  )

  val voluntaryReasonRow: SummaryRow = SummaryRow(
    s"$sectionId.voluntaryRegistrationReason",
    vatTradingDetails.flatMap(_.vatChoice.reason).collect {
      case VoluntaryRegistrationReason.SELLS => "pages.voluntary.registration.reason.radio.sells"
      case VoluntaryRegistrationReason.INTENDS_TO_SELL => "pages.voluntary.registration.reason.radio.intendsToSell"
    }.getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationReasonController.show())
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(NECESSITY_VOLUNTARY, VatStartDate(_, Some(date)), _, _) => date.format(presentationFormatter)
    }.getOrElse(s"pages.summary.$sectionId.mandatoryStartDate"),
    if (voluntaryRegistration) Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()) else None
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    vatTradingDetails.flatMap(_.tradingName.tradingName).getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.routes.TradingNameController.show())
  )

  val section: SummarySection =
    SummarySection(
      sectionId,
      rows = Seq(
        (taxableTurnoverRow, !vatTradingDetails.flatMap(_.vatChoice.vatThresholdPostIncorp).isDefined),
        (overThresholdSelectionRow, vatTradingDetails.flatMap(_.vatChoice.vatThresholdPostIncorp).isDefined),
        (overThresholdDateRow, vatTradingDetails.flatMap(_.vatChoice.vatThresholdPostIncorp).flatMap(_.overThresholdDate).isDefined),
        (necessityRow, voluntaryRegistration),
        (voluntaryReasonRow, voluntaryRegistration),
        (startDateRow, true),
        (tradingNameRow, true)
      )
    )

}
