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

  val validHttpResponse = HttpResponse(200)

  val validDateTime = {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
    formatter.parseDateTime("01/02/2017")
  }

  // TODO: remove when we play the VatChoice refactoring story
  val validDefaultDateTime = {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
    formatter.parseDateTime("01/01/1970")
  }

  val validStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(1), Some(2), Some(2017))
  val differentStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(30), Some(12), Some(2001))

  val validTradingName = TradingName(TradingName.TRADING_NAME_YES, Some("ACME INC"))
  val differentTradingName = TradingName(TradingName.TRADING_NAME_YES, Some("HOLIDAY INC"))

  val validEstimateVatTurnover = EstimateVatTurnover(Some(50000L))
  val differentEstimateVatTurnover = EstimateVatTurnover(Some(10000L))

  val validEstimateZeroRatedSales = EstimateZeroRatedSales(Some(60000L))
  val differentEstimateZeroRatedSales = EstimateZeroRatedSales(Some(20000L))

  val validRegId = "VAT123456"

  val validVatChoice = VatChoice(
    validStartDate.toDateTime,
    VatChoice.NECESSITY_VOLUNTARY
  )

  val validVatFinancials = VatFinancials(Some(VatBankAccount("ACME", "101010","100000000000")),
    10000000000L,
    Some(10000000000L),
    true,
    VatAccountingPeriod(None, "monthly")
  )

  val differentVatChoice = VatChoice(
    differentStartDate.toDateTime,
    VatChoice.NECESSITY_VOLUNTARY
  )

  val validVatTradingDetails = VatTradingDetails(
    "ACME INC"
  )

  val differentVatTradingDetails = VatTradingDetails(
    "HOLIDAY INC"
  )

  val validNewVatScheme = VatScheme.blank(validRegId)

  val validVatScheme = VatScheme(
    validRegId,
    Some(validVatTradingDetails),
    Some(validVatChoice),
    Some(validVatFinancials)
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
        id = "vatDetails.taxableTurnover",
        answer = Right("No"),
        changeLink = Some(controllers.userJourney.routes.TaxableTurnoverController.show())
      ),
      SummaryRow(
        id = "vatDetails.necessity",
        answer = Right("Yes"),
        changeLink = Some(controllers.userJourney.routes.VoluntaryRegistrationController.show())
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
        "companyDetails.tradingName",
        Right("ACME INC"),
        Some(controllers.userJourney.routes.TradingNameController.show())
      ),
      SummaryRow(
        "companyDetails.estimatedSalesValue",
        Right("£10000000000"),
        Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
      ),
      SummaryRow(
        "companyDetails.zeroRatedSales",
        Right("Yes"),
        Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
      ),
      SummaryRow(
        "companyDetails.zeroRatedSalesValue",
        Right("£10000000000"),
        Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
      )
    )
  )
}
