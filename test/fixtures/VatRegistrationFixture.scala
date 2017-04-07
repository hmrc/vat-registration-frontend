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

import models.api.{VatComplianceCultural, _}
import models.view._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import models.view.vatTradingDetails.vatChoice.StartDateView
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
  val someTestDate = Some(LocalDate.of(2017, 3, 21))
  val vatStartDate = VatStartDate(StartDateView.SPECIFIC_DATE, someTestDate)

  val validVatChoice = VatChoice(VatChoice.NECESSITY_VOLUNTARY, vatStartDate)

  val tradingName: String = "ACME INC"
  val validTradingName = TradingName(selection = true, tradingName = Some(tradingName))
  val validEuTrading = VatEuTrading(selection = false, eoriApplication = None)
  val validVatTradingDetails = VatTradingDetails(vatChoice = validVatChoice, tradingName = validTradingName, validEuTrading)

  private val turnoverEstimate = 50000L
  private val estimatedSales = 60000L

  val sortCode = "12-34-56"
  val accountNumber = "12345678"
  val businessActivityDescription = "description"

  val validEstimateVatTurnover = EstimateVatTurnover(turnoverEstimate)
  val validEstimateZeroRatedSales = EstimateZeroRatedSales(estimatedSales)
  val validVatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
  val validVatReturnFrequency = VatReturnFrequency(VatReturnFrequency.QUARTERLY)
  val validAccountingPeriod = AccountingPeriod(AccountingPeriod.MAR_JUN_SEP_DEC)
  val validBankAccountDetails = CompanyBankAccountDetails(tradingName, accountNumber, sortCode)
  val monthlyAccountingPeriod = VatAccountingPeriod(frequency = "monthly")
  val validBankAccount = VatBankAccount(tradingName, accountNumber, sortCode)

  val validVatFinancials = VatFinancials(
    bankAccount = Some(validBankAccount),
    turnoverEstimate = turnoverEstimate,
    zeroRatedTurnoverEstimate = Some(estimatedSales),
    reclaimVatOnMostReturns = true,
    accountingPeriods = monthlyAccountingPeriod
  )

  val validSicAndCompliance = VatSicAndCompliance(
    businessDescription = businessActivityDescription,
    culturalCompliance = None,
    labourCompliance = None
  )

  val emptyVatScheme = VatScheme(validRegId)

  def tradingDetails(
                      necessity: String = VatChoice.NECESSITY_VOLUNTARY,
                      startDateSelection: String = StartDateView.COMPANY_REGISTRATION_DATE,
                      startDate: Option[LocalDate] = None,
                      tradingNameSelection: Boolean = true,
                      tradingName: Option[String] = Some("ACME Ltd."),
                      reason: Option[String] = None,
                      euGoodsSelection: Boolean = true,
                      eoriApplication: Option[Boolean] = None
                    ): VatTradingDetails = VatTradingDetails(
    vatChoice = VatChoice(
      necessity = necessity,
      vatStartDate = VatStartDate(
        selection = startDateSelection,
        startDate = startDate
      ),
      reason = reason
    ),
    tradingName = TradingName(
      selection = tradingNameSelection,
      tradingName = tradingName
    ),
    euTrading = VatEuTrading(
      euGoodsSelection,
      eoriApplication
    )
  )

  def vatSicAndCompliance(
                           activityDescription: String = "Some business activity",
                           culturalComplianceSection: Option[VatComplianceCultural] = Some(VatComplianceCultural(notForProfit = false)),
                           labourComplianceSection: Option[VatComplianceLabour] = Some(VatComplianceLabour(true, Some(8), Some(true), Some(true)))

                         ): VatSicAndCompliance =
    VatSicAndCompliance(businessDescription = activityDescription, culturalCompliance = culturalComplianceSection, labourCompliance = labourComplianceSection)


  def vatScheme(
                 id: String = validRegId,
                 vatTradingDetails: Option[VatTradingDetails] = None,
                 sicAndCompliance: Option[VatSicAndCompliance] = None
               ): VatScheme = VatScheme(
    id = id,
    tradingDetails = vatTradingDetails,
    vatSicAndCompliance = sicAndCompliance
  )

  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    id = validRegId,
    financials = Some(
      VatFinancials(
        bankAccount = None,
        turnoverEstimate = 0L,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = false,
        accountingPeriods = VatAccountingPeriod(VatReturnFrequency.MONTHLY))
    )
  )

  val validVatScheme = VatScheme(
    id = validRegId,
    tradingDetails = Some(validVatTradingDetails),
    financials = Some(validVatFinancials)
  )

  val validBusinessActivityDescription = BusinessActivityDescription(businessActivityDescription)
  val validVatCulturalCompliance = VatComplianceCultural(true)

  //Cultural Compliance Questions
  val validNotForProfit = NotForProfit(NotForProfit.NOT_PROFIT_NO)

  //Labour Compliance Questions
  val validCompanyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)
  val validWorkers = Workers(8)
  val validTemporaryContracts = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)
  val validSkilledWorkers = SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO)

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
        changeLink = Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.necessity",
        answerMessageKey = "Yes",
        changeLink = Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show())
      ), true),
      (SummaryRow(
        id = "vatDetails.startDate",
        answerMessageKey = "1 February 2017",
        changeLink = Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show())
      ), true)
    )
  )

  private def getCompanyDetailsSection: SummarySection = SummarySection(
    id = "companyDetails",
    Seq(
      (SummaryRow(
        "companyDetails.tradingName",
        tradingName,
        Some(controllers.vatTradingDetails.routes.TradingNameController.show())
      ), true),
      (SummaryRow(
        "companyDetails.estimatedSalesValue",
        "£10000000000",
        Some(controllers.vatFinancials.routes.EstimateVatTurnoverController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSales",
        "Yes",
        Some(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
      ), true),
      (SummaryRow(
        "companyDetails.zeroRatedSalesValue",
        "£10000000000",
        Some(controllers.vatFinancials.routes.EstimateZeroRatedSalesController.show())
      ), true)
    )
  )
}
