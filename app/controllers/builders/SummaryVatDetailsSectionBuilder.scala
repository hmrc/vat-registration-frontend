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

import java.time.format.DateTimeFormatter

import models.api.VatChoice.NECESSITY_VOLUNTARY
import models.api.{VatChoice, VatStartDate, VatTradingDetails}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.view.{SummaryRow, SummarySection}

case class SummaryVatDetailsSectionBuilder(vatTradingDetails: Option[VatTradingDetails] = None)
  extends SummarySectionBuilder {

  private val voluntaryRegistration = vatTradingDetails.exists(_.registeringVoluntarily)

  def taxableTurnoverRow: SummaryRow = SummaryRow(
    "vatDetails.taxableTurnover",
    s"app.common.${if (voluntaryRegistration) "no" else "yes"}",
    Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
  )

  def necessityRow: SummaryRow = SummaryRow(
    "vatDetails.necessity",
    s"app.common.${if (voluntaryRegistration) "yes" else "no"}",
    if (voluntaryRegistration) Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show()) else None
  )

  def voluntaryReasonRow: SummaryRow = SummaryRow(
    "vatDetails.voluntaryRegistrationReason",
    vatTradingDetails.flatMap(_.vatChoice.reason).collect{
      case VoluntaryRegistrationReason.SELLS => "pages.voluntary.registration.reason.radio.sells"
      case VoluntaryRegistrationReason.INTENDS_TO_SELL => "pages.voluntary.registration.reason.radio.intendsToSell"
    }.getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationReasonController.show())
  )

  val presentationFormatter = DateTimeFormatter.ofPattern("d MMMM y")

  def startDateRow: SummaryRow = SummaryRow(
    "vatDetails.startDate",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(NECESSITY_VOLUNTARY, VatStartDate(_, Some(date)), _) => date.format(presentationFormatter)
    }.getOrElse("pages.summary.vatDetails.mandatoryStartDate"),
    if (voluntaryRegistration) Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()) else None
  )

  def tradingNameRow: SummaryRow = SummaryRow(
    "vatDetails.tradingName",
    vatTradingDetails.flatMap(_.tradingName.tradingName).getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.routes.TradingNameController.show())
  )

  def section: SummarySection =
    SummarySection(id = "vatDetails",
      rows = Seq(
        (taxableTurnoverRow, true),
        (necessityRow, voluntaryRegistration),
        (voluntaryReasonRow, voluntaryRegistration),
        (startDateRow, true),
        (tradingNameRow, true)
      )
    )

}
