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

import models.api.{VatChoice, VatTradingDetails}
import models.view.{SummaryRow, SummarySection}

case class SummaryVatDetailsSectionBuilder(vatChoice: VatChoice, vatTradingDetails: VatTradingDetails)
  extends SummarySectionBuilder {

  def taxableTurnoverRow: SummaryRow = SummaryRow(
    "vatDetails.taxableTurnover",
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => "app.common.no"
      case _ => "app.common.yes"
    },
    Some(controllers.userJourney.routes.TaxableTurnoverController.show())
  )

  def necessityRow: SummaryRow = SummaryRow(
    "vatDetails.necessity",
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => "app.common.yes"
      case _ => "app.common.no"
    },
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.VoluntaryRegistrationController.show())
      case _ => None
    }
  )

  def startDateRow: SummaryRow = SummaryRow(
    "vatDetails.startDate",
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY =>
        val startdate = vatChoice.startDate.toString("dd/MM/yyyy")
        if (startdate == "31/12/1969" || startdate == "01/01/1970") {
          "pages.summary.vatDetails.mandatoryStartDate"
        } else {
          vatChoice.startDate.toString("d MMMM y")
        }
      case _ => "pages.summary.vatDetails.mandatoryStartDate"
    },
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.StartDateController.show())
      case _ => None
    }
  )

  def tradingNameRow: SummaryRow = SummaryRow(
    "vatDetails.tradingName",
    vatTradingDetails.tradingName match {
      case "" => "app.common.no"
      case _ => vatTradingDetails.tradingName
    },
    Some(controllers.userJourney.routes.TradingNameController.show())
  )

  def section: SummarySection = SummarySection(
      id = "vatDetails",
      Seq(
        (taxableTurnoverRow, true),
        (necessityRow, vatChoice.necessity == VatChoice.NECESSITY_VOLUNTARY),
        (startDateRow, true),
        (tradingNameRow, true)

      )
    )
}
