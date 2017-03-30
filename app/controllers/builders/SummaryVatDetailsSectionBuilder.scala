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

import models.api.{VatChoice, VatStartDate, VatTradingDetails}
import models.view.{SummaryRow, SummarySection}

case class SummaryVatDetailsSectionBuilder(vatTradingDetails: Option[VatTradingDetails] = None)
  extends SummarySectionBuilder {

  def taxableTurnoverRow: SummaryRow = SummaryRow(
    "vatDetails.taxableTurnover",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatChoice.NECESSITY_VOLUNTARY, _) => "app.common.no"
    }.getOrElse("app.common.yes"),
    Some(controllers.userJourney.vatChoice.routes.TaxableTurnoverController.show())
  )

  def necessityRow: SummaryRow = SummaryRow(
    "vatDetails.necessity",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatChoice.NECESSITY_VOLUNTARY, _) => "app.common.yes"
    }.getOrElse("app.common.no"),
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatChoice.NECESSITY_VOLUNTARY, _) =>
        controllers.userJourney.vatChoice.routes.VoluntaryRegistrationController.show()
    }
  )

  val presentationFormatter = DateTimeFormatter.ofPattern("d MMMM y")

  def startDateRow: SummaryRow = SummaryRow(
    "vatDetails.startDate",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatChoice.NECESSITY_VOLUNTARY, VatStartDate(_, Some(date))) => date.format(presentationFormatter)
    }.getOrElse("pages.summary.vatDetails.mandatoryStartDate"),
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatChoice.NECESSITY_VOLUNTARY, _) => controllers.userJourney.vatChoice.routes.StartDateController.show()
    }
  )

  def tradingNameRow: SummaryRow = SummaryRow(
    "vatDetails.tradingName",
    vatTradingDetails.flatMap(_.tradingName.tradingName).getOrElse("app.common.no"),
    Some(controllers.userJourney.vatTradingDetails.routes.TradingNameController.show())
  )

  def section: SummarySection = SummarySection(
    id = "vatDetails",
    Seq(
      (taxableTurnoverRow, true),
      (necessityRow, vatTradingDetails.map(_.vatChoice).exists(_.necessity == VatChoice.NECESSITY_VOLUNTARY)),
      (startDateRow, true),
      (tradingNameRow, true)
    )
  )
}
