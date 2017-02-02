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

package fixtures

import models.api._
import models.view.{Summary, SummaryRow, SummarySection}
import org.joda.time.format.DateTimeFormat

trait VatRegistrationFixture {

  val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
  val dateTime = formatter.parseDateTime("01/11/2017 15:10:12")

  val validVatScheme = VatScheme(
    "VAT123456",
    VatTradingDetails("ACME INC"),
    VatChoice(dateTime, VatChoice.NECESSITY_VOLUNTARY)
  )

  lazy val validSummaryView = Summary(
    Seq(
      getVatDetailsSection,
      getCompanyDetailsSection
    )
  )

  private def getVatDetailsSection: SummarySection = SummarySection(
    id = "vatDetails",
    Seq(
      SummaryRow(
        id = "vatDetails.registerVoluntarily",
        answer = Right("Yes"),
        changeLink = Some(controllers.userJourney.routes.SummaryController.show())
      ),
      SummaryRow(
        id = "vatDetails.startDate",
        answer = Right("1 February 2017"),
        changeLink = Some(controllers.userJourney.routes.StartDateController.show())
      )
    )
  )

  private def getCompanyDetailsSection: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      SummaryRow(
        "vatDetails.tradingName",
        Right("ACME INC"),
        Some(controllers.userJourney.routes.TradingNameController.show())
      )
    )
  )
}
