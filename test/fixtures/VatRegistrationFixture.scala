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

import java.time.LocalDate

import models.api._
import models.api.compliance.VatCulturalCompliance
import models.view._
import models.view.sicAndCompliance.{BusinessActivityDescription, CulturalComplianceQ1}
import models.view.vatChoice.StartDate
import models.view.vatFinancials._
import models.view.vatTradingDetails.TradingName
import play.api.http.Status._
import uk.gov.hmrc.play.http._

trait VatRegistrationFixture {

  val contextRoot = "/register-for-vat"

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

  val validStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(LocalDate.of(2017, 3, 21)))

  val validVatChoice = VatChoice(
    validStartDate.date.get,
    VatChoice.NECESSITY_VOLUNTARY
  )

  private val tradingName = "ACME INC"
  val validVatTradingDetails = VatTradingDetails(tradingName)
  val validTradingName = TradingName(TradingName.TRADING_NAME_YES, Some(tradingName))

  private val turnoverEstimate = 50000L
  private val estimatedSales = 60000L

  private val sortCode = "10-10-10"
  private val accountNumber = "12345678"
  private val period = "monthly"
  private val businessActivityDescription = "description"

  val validEstimateVatTurnover = EstimateVatTurnover(turnoverEstimate)
  val validEstimateZeroRatedSales = EstimateZeroRatedSales(estimatedSales)
  val validVatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
  val validVatReturnFrequency = VatReturnFrequency(VatReturnFrequency.QUARTERLY)
  val validAccountingPeriod = AccountingPeriod(AccountingPeriod.MAR_JUN_SEP_DEC)
  val validBankAccountDetails = CompanyBankAccountDetails(tradingName, accountNumber, sortCode)

  val validVatFinancials = VatFinancials(
    bankAccount = Some(VatBankAccount(tradingName, accountNumber, sortCode)),
    turnoverEstimate = turnoverEstimate,
    zeroRatedSalesEstimate = Some(estimatedSales),
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, period)
  )

  val validSicAndCompliance = VatSicAndCompliance(
    businessDescription = businessActivityDescription,
    culturalCompliance = None
  )

  val emptyVatScheme = VatScheme.blank(validRegId)

  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    id = validRegId,
    tradingDetails = None,
    vatChoice = None,
    financials = Some(
      VatFinancials(None, 0L, None, reclaimVatOnMostReturns = false, VatAccountingPeriod(None, VatReturnFrequency.MONTHLY))
    )
  )

  val validVatScheme = VatScheme(
    id = validRegId,
    tradingDetails = Some(validVatTradingDetails),
    vatChoice = Some(validVatChoice),
    financials = Some(validVatFinancials)
  )

  val validBusinessActivityDescription = BusinessActivityDescription(businessActivityDescription)
  val validVatCulturalCompliance = VatCulturalCompliance(true)
  val validCulturalComplianceQ1 = CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_NO)

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
        answerMessageKey = "No",
        changeLink = Some(controllers.userJourney.vatChoice.routes.TaxableTurnoverController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.necessity",
        answerMessageKey = "Yes",
        changeLink = Some(controllers.userJourney.vatChoice.routes.VoluntaryRegistrationController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.startDate",
        answerMessageKey = "1 February 2017",
        changeLink = Some(controllers.userJourney.vatChoice.routes.StartDateController.show())
      ), true)
    )
  )

  private def getCompanyDetailsSection: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      (SummaryRow(
        "companyDetails.tradingName",
        tradingName,
        Some(controllers.userJourney.vatTradingDetails.routes.TradingNameController.show())
      ), true),
      (SummaryRow(
        "companyDetails.estimatedSalesValue",
        "£10000000000",
        Some(controllers.userJourney.vatFinancials.routes.EstimateVatTurnoverController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSales",
        "Yes",
        Some(controllers.userJourney.vatFinancials.routes.ZeroRatedSalesController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSalesValue",
        "£10000000000",
        Some(controllers.userJourney.vatFinancials.routes.EstimateZeroRatedSalesController.show())
      ), true)
    )
  )
}
