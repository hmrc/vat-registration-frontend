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
import play.api.http.Status._
import uk.gov.hmrc.play.http._

trait VatRegistrationFixture {

  val IM_A_TEAPOT = 418
  val badRequest = new BadRequestException(BAD_REQUEST.toString)
  val forbidden = Upstream4xxResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")

  val validHttpResponse = HttpResponse(OK)

  val validRegId = "VAT123456"

  val validStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(1), Some(2), Some(2017))

  val validVatChoice = VatChoice(
    validStartDate.toDateTime,
    VatChoice.NECESSITY_VOLUNTARY
  )

  private val tradingName = "ACME INC"
  val validVatTradingDetails = VatTradingDetails(tradingName)
  val validTradingName = TradingName(TradingName.TRADING_NAME_YES, Some(tradingName))

  private val turnoverEstimate = 50000L
  private val estimatedSales = 60000L
  val validEstimateVatTurnover = EstimateVatTurnover(turnoverEstimate)
  val validEstimateZeroRatedSales = EstimateZeroRatedSales(estimatedSales)
  val validVatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
  val validVatReturnFrequency = VatReturnFrequency(VatReturnFrequency.QUARTERLY)
  val validAccountingPeriod = AccountingPeriod(AccountingPeriod.MAR_JUN_SEP_DEC)
  val validBankAccountDetails = CompanyBankAccountDetails(tradingName, accountNumber, sortCode)

  private val sortCode = "10-10-10"
  private val accountNumber = "12345678"
  private val period = "monthly"
  private val businessActivityDescription = "description"


  val validVatFinancials = VatFinancials(
    bankAccount = Some(VatBankAccount(tradingName, accountNumber, sortCode)),
    turnoverEstimate = turnoverEstimate,
    zeroRatedSalesEstimate = Some(estimatedSales),
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, period)
  )


  val validSicAndCompliance = SicAndCompliance(
    description = businessActivityDescription
  )

  val emptyVatScheme = VatScheme.blank(validRegId)

  val validVatScheme = VatScheme(
    id = validRegId,
    tradingDetails = Some(validVatTradingDetails),
    vatChoice = Some(validVatChoice),
    financials = Some(validVatFinancials)
  )

  val validBusinessActivityDescription = BusinessActivityDescription()

  lazy val validSummaryView = Summary(
    Seq(
      getVatDetailsSection,
      getCompanyDetailsSection
    )
  )

  private def getVatDetailsSection: SummarySection = SummarySection(
    id = "vatDetails",
    Seq(
      (SummaryRow(
        id = "vatDetails.taxableTurnover",
        answer = Right("No"),
        changeLink = Some(controllers.userJourney.routes.TaxableTurnoverController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.necessity",
        answer = Right("Yes"),
        changeLink = Some(controllers.userJourney.routes.VoluntaryRegistrationController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.startDate",
        answer = Right("1 February 2017"),
        changeLink = Some(controllers.userJourney.routes.StartDateController.show())
      ), true)
    )
  )

  private def getCompanyDetailsSection: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      (SummaryRow(
        "companyDetails.tradingName",
        Right(tradingName),
        Some(controllers.userJourney.routes.TradingNameController.show())
      ), true),
      (SummaryRow(
        "companyDetails.estimatedSalesValue",
        Right("£10000000000"),
        Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSales",
        Right("Yes"),
        Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSalesValue",
        Right("£10000000000"),
        Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
      ), true)
    )
  )
}
