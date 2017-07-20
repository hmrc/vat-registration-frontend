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
import models.external.{CoHoCompanyProfile, Officer}
import models.view.frs._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial.{ActAsIntermediary, AdviceOrConsultancy}
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatLodgingOfficer.OfficerContactDetailsView
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
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
  val NINO: String = "AA 12 34 56 C"

  val validHttpResponse = HttpResponse(OK)

  val validRegId = "VAT123456"
  val someTestDate = Some(LocalDate.of(2017, 3, 21))
  val vatStartDate = VatStartDate(StartDateView.SPECIFIC_DATE, someTestDate)
  val validStartDateView = StartDateView(StartDateView.SPECIFIC_DATE, someTestDate)

  val validVatChoice = VatChoice(VatChoice.NECESSITY_VOLUNTARY, vatStartDate)

  val tradingName: String = "ACME INC"
  val validTradingName = TradingName(selection = true, tradingName = Some(tradingName))
  val validTradingNameView = TradingNameView("TRADING_NAME_YES", Some("Test Trading Name"))
  val validEuTrading = VatEuTrading(selection = false, eoriApplication = None)
  val validVatTradingDetails = VatTradingDetails(vatChoice = validVatChoice, tradingName = validTradingName, validEuTrading)

  val validVatContact = VatContact(VatDigitalContact(email = "asd@com", tel = Some("123"), mobile = None), website = None)

  private val turnoverEstimate = 50000L
  private val estimatedSales = 60000L

  val sortCode = "12-34-56"
  val accountNumber = "12345678"
  val businessActivityDescription = "description"

  val validTaxableTurnover = TaxableTurnover("TAXABLE_YES")
  val validEstimateVatTurnover = EstimateVatTurnover(turnoverEstimate)
  val validEstimateZeroRatedSales = EstimateZeroRatedSales(estimatedSales)
  val validVatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
  val validVatReturnFrequency = VatReturnFrequency(VatReturnFrequency.QUARTERLY)
  val validAccountingPeriod = AccountingPeriod(AccountingPeriod.MAR_JUN_SEP_DEC)
  val validBankAccountDetails = CompanyBankAccountDetails(tradingName, accountNumber, sortCode)
  val monthlyAccountingPeriod = VatAccountingPeriod(frequency = "monthly")
  val validBankAccount = VatBankAccount(tradingName, accountNumber, sortCode)
  val validCompanyBankAccount = CompanyBankAccount.yes

  val validVatFinancials = VatFinancials(
    bankAccount = Some(validBankAccount),
    turnoverEstimate = turnoverEstimate,
    zeroRatedTurnoverEstimate = Some(estimatedSales),
    reclaimVatOnMostReturns = true,
    accountingPeriods = monthlyAccountingPeriod
  )

  val sicCode = SicCode("88888888", "description", "displayDetails")
  val validSicAndCompliance = VatSicAndCompliance(
    businessDescription = businessActivityDescription,
    culturalCompliance = Some(VatComplianceCultural(notForProfit = false)),
    labourCompliance = None,
    financialCompliance = None,
    mainBusinessActivity = sicCode
  )

  val validDob = DateOfBirth(12, 11, 1973)
  val validStartDate = DateOfBirth(12, 11, 1990)


  val officer = Officer(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director", Some(validDob), None, None)
  val completionCapacity = CompletionCapacity(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director")


  val validServiceEligibility = VatServiceEligibility(Some(true), Some(false), Some(false), Some(false), Some(false))
  val officerName = Name(Some("Reddy"), None, "Yattapu", Some("Dr"))
  val validOfficerContactDetailsView = OfficerContactDetailsView(Some("test@test.com"), Some("07837483287"), Some("07827483287"))
  val validOfficerContactDetails = OfficerContactDetails(Some("test@test.com"), None, None)
  val changeOfName = ChangeOfName(true, None)
  val currentOrPreviousAddress = CurrentOrPreviousAddress(false, Some(ScrsAddress("", "")))
  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))

  val validLodgingOfficer = VatLodgingOfficer(
    ScrsAddress("", ""),
    DateOfBirth.empty,
    "", "director",
    officerName,
    changeOfName,
    currentOrPreviousAddress,
    validOfficerContactDetails
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
                           culturalComplianceSection: Option[VatComplianceCultural] = Some(VatComplianceCultural(
                             notForProfit = false)),
                           labourComplianceSection: Option[VatComplianceLabour] = Some(VatComplianceLabour(
                             labour = true,
                             workers = Some(8),
                             temporaryContracts = Some(true),
                             skilledWorkers = Some(true))),
                           financialComplianceSection: Option[VatComplianceFinancial] = Some(VatComplianceFinancial(
                             adviceOrConsultancyOnly = true,
                             actAsIntermediary = false,
                             chargeFees = Some(true),
                             additionalNonSecuritiesWork = Some(true))),
                           mainBusinessActivitySection: SicCode): VatSicAndCompliance =
    VatSicAndCompliance(
      businessDescription = activityDescription,
      culturalCompliance = culturalComplianceSection,
      labourCompliance = labourComplianceSection,
      financialCompliance = financialComplianceSection,
      mainBusinessActivity = mainBusinessActivitySection
    )


  def vatScheme(
                 id: String = validRegId,
                 vatTradingDetails: Option[VatTradingDetails] = None,
                 sicAndCompliance: Option[VatSicAndCompliance] = None,
                 contact: Option[VatContact] = None,
                 vatFlatRateScheme: Option[VatFlatRateScheme] = None
               ): VatScheme = VatScheme(
    id = id,
    tradingDetails = vatTradingDetails,
    vatSicAndCompliance = sicAndCompliance,
    vatContact = contact,
    vatFlatRateScheme = vatFlatRateScheme
  )

  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    id = validRegId,
    vatSicAndCompliance = Some(validSicAndCompliance),
    financials = Some(
      VatFinancials(
        bankAccount = None,
        turnoverEstimate = 0L,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = false,
        accountingPeriods = VatAccountingPeriod(VatReturnFrequency.MONTHLY))
    )
  )

  val validCoHoProfile = CoHoCompanyProfile("status", "transactionId")

  val validBusinessActivityDescription = BusinessActivityDescription(businessActivityDescription)
  val validVatCulturalCompliance = VatComplianceCultural(notForProfit = true)
  val validVatLabourCompliance = VatComplianceLabour(labour = false)
  val validVatFinancialCompliance = VatComplianceFinancial(adviceOrConsultancyOnly = false, actAsIntermediary = false)

  //Cultural Compliance Questions
  val validNotForProfit = NotForProfit(NotForProfit.NOT_PROFIT_NO)

  //Labour Compliance Questions
  val validCompanyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)
  val validWorkers = Workers(8)
  val validTemporaryContracts = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)
  val validSkilledWorkers = SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO)

  //Financial Compliance Questions
  val validAdviceOrConsultancy = AdviceOrConsultancy(true)
  val validActAsIntermediary = ActAsIntermediary(true)

  val validEuGoods = EuGoods(EuGoods.EU_GOODS_YES)
  val validApplyEori = ApplyEori(ApplyEori.APPLY_EORI_YES)

  val validBusinessContactDetails = BusinessContactDetails(email = "test@foo.com", daytimePhone = Some("123"), mobile = None, website = None)
  val validBusinessSectorView = BusinessSectorView("test business sector", 3.14)

  val validVatFlatRateScheme = VatFlatRateScheme(
    joinFrs = true,
    annualCostsInclusive = Some(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS),
    annualCostsLimited = Some(AnnualCostsLimitedView.YES_WITHIN_12_MONTHS),
    doYouWantToUseThisRate = Some(false),
    categoryOfBusiness = Some(validBusinessSectorView.businessSector),
    percentage = Some(BigDecimal(3.14))
  )

  val validVatScheme = VatScheme(
    id = validRegId,
    tradingDetails = Some(validVatTradingDetails),
    financials = Some(validVatFinancials),
    vatContact = Some(validVatContact),
    lodgingOfficer = Some(validLodgingOfficer),
    vatSicAndCompliance = Some(validSicAndCompliance),
    vatFlatRateScheme = Some(validVatFlatRateScheme)
  )

}
