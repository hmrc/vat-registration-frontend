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
import models.view._
import org.joda.time.format.DateTimeFormat
import play.api.http.Status
import uk.gov.hmrc.play.http._

trait VatRegistrationFixture {

  val IM_A_TEAPOT: Int = 418
  val badRequest = new BadRequestException(Status.BAD_REQUEST.toString)
  val forbidden = Upstream4xxResponse(Status.FORBIDDEN.toString, Status.FORBIDDEN, Status.FORBIDDEN)
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(Status.INTERNAL_SERVER_ERROR.toString, Status.INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR)
  val notFound = new NotFoundException(Status.NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(Status.BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")

  val validDateTime = {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
    formatter.parseDateTime("01/02/17")
  }

  val validStartDate = StartDate(StartDate.SPECIFIC_DATE, Some("01"), Some("02"), Some("17"))
  val validTradingName = TradingName(TradingName.TRADING_NAME_YES, Some("ACME INC"))
  val validRegId = "VAT123456"

  val validVatChoice = VatChoice(
    validStartDate.toDate,
    VatChoice.NECESSITY_VOLUNTARY
  )

  val validVatChoice2 = VatChoice(
    validStartDate.toDate,
    VatChoice.NECESSITY_OBLIGATORY
  )

  val validVatTradingDetails = VatTradingDetails(
    "ACME INC"
  )

  val validNewVatScheme = VatScheme.blank(validRegId)

  val validVatScheme = VatScheme(
    validRegId,
    validVatTradingDetails,
    validVatChoice
  )

  val validVatScheme2 = VatScheme(
    validRegId,
    validVatTradingDetails,
    validVatChoice2
  )

  lazy val validSummaryView = Summary(
    Seq(
      getVatDetailsSection,
      getCompanyDetailsSection
    )
  )

  lazy val validSummaryView2 = Summary(
    Seq(
      getVatDetailsSection2,
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
        answer = Right("1 February 17"),
        changeLink = Some(controllers.userJourney.routes.StartDateController.show())
      )
    )
  )

  private def getVatDetailsSection2: SummarySection = SummarySection(
    id = "vatDetails",
    Seq(
      SummaryRow(
        id = "vatDetails.registerVoluntarily",
        answer = Left("No"),
        changeLink = Some(controllers.userJourney.routes.SummaryController.show())
      ),
      SummaryRow(
        id = "vatDetails.startDate",
        answer = Right("1 February 17"),
        changeLink = Some(controllers.userJourney.routes.StartDateController.show())
      )
    )
  )

  private def getCompanyDetailsSection: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      SummaryRow(
        "companyDetails.tradingName",
        Right("ACME INC"),
        Some(controllers.userJourney.routes.TradingNameController.show())
      )
    )
  )
}
