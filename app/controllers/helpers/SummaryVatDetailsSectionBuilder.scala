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

package controllers.helpers

import javax.inject.Inject

import controllers.CommonPlayDependencies
import models.api.{VatChoice, VatFinancials, VatTradingDetails}
import models.view.{SummaryRow, SummarySection}

class SummaryVatDetailsSectionBuilder(ds: CommonPlayDependencies)(vatChoice: VatChoice)
  extends SummarySectionBuilder(ds) {

  def taxableTurnoverRow: SummaryRow = SummaryRow(
    "vatDetails.taxableTurnover",
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => Right(messagesApi("app.common.no"))
      case _ => Right(messagesApi("app.common.yes"))
    },
    Some(controllers.userJourney.routes.TaxableTurnoverController.show())
  )

  def necessityRow: SummaryRow = SummaryRow(
    "vatDetails.necessity",
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => Right(messagesApi("app.common.yes"))
      case _ => Right(messagesApi("app.common.no"))
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
          Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
        } else {
          Right(vatChoice.startDate.toString("d MMMM y"))
        }
      case _ => Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
    },
    vatChoice.necessity match {
      case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.StartDateController.show())
      case _ => None
    }
  )

  def summarySection: SummarySection = SummarySection(
      id = "vatDetails",
      Seq(
        (taxableTurnoverRow, true),
        (necessityRow, vatChoice.necessity == VatChoice.NECESSITY_VOLUNTARY),
        (startDateRow, true)
      )
    )
}
