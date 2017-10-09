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
import java.time.format.DateTimeFormatter

import models.api._
import models.external.{IncorporationInfo, _}
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial.{ActAsIntermediary, AdviceOrConsultancy}
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatContact.BusinessContactDetails
import models.view.vatLodgingOfficer.OfficerContactDetailsView
import play.api.http.Status._
import uk.gov.hmrc.play.http._

trait BaseFixture {
  //Test variables
  val testDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
}

trait VatRegistrationFixture extends FlatRateFixture with TradingDetailsFixture with FinancialsFixture {

  //Responses
  val IM_A_TEAPOT = 418
  val forbidden = Upstream4xxResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val validHttpResponse = HttpResponse(OK)

  //Exceptions
  val badRequest = new BadRequestException(BAD_REQUEST.toString)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")

  //Test variables
  val contextRoot = "/register-for-vat"
  val testNino: String = "AA 12 34 56 C"
  val testBusinessActivityDescription = "description"
  val testRegId = "VAT123456"
  val testMonthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")

  //View models
  val validOfficerContactDetailsView = OfficerContactDetailsView(Some("test@test.com"), Some("07837483287"), Some("07827483287"))
  val validNotForProfit = NotForProfit(NotForProfit.NOT_PROFIT_NO)
  val validCompanyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)
  val validWorkers = Workers(8)
  val validTemporaryContracts = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)
  val validSkilledWorkers = SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO)
  val validAdviceOrConsultancy = AdviceOrConsultancy(true)
  val validActAsIntermediary = ActAsIntermediary(true)
  val validBusinessActivityDescription = BusinessActivityDescription(testBusinessActivityDescription)
  val validBusinessContactDetails = BusinessContactDetails(email = "test@foo.com", daytimePhone = Some("123"), mobile = None, website = None)

  //Api models
  val sicCode = SicCode("88888888", "description", "displayDetails")
  val validDob = DateOfBirth(12, 11, 1973)
  val validStartDate = DateOfBirth(12, 11, 1990)
  val officer = Officer(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director", Some(validDob), None, None)
  val completionCapacity = CompletionCapacity(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director")
  def validServiceEligibility(nes : String = VatEligibilityChoice.NECESSITY_VOLUNTARY, reason : Option[String] = None) =
      VatServiceEligibility(
        Some(true),
        Some(false),
        Some(false),
        Some(false),
        Some(false),
        Some(false),
        Some(VatEligibilityChoice(
          nes,
          reason,
          None)))

  val officerName = Name(Some("Reddy"), None, "Yattapu", Some("Dr"))
  val validOfficerContactDetails = OfficerContactDetails(Some("test@test.com"), None, None)
  val changeOfName = ChangeOfName(true, None)
  val currentOrPreviousAddress = CurrentOrPreviousAddress(false, Some(ScrsAddress("", "")))
  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))
  val validVatThresholdPostIncorp = VatThresholdPostIncorp(overThresholdSelection = false, None)
  val validVatCulturalCompliance = VatComplianceCultural(notForProfit = true)
  val validVatLabourCompliance = VatComplianceLabour(labour = false)
  val validVatFinancialCompliance = VatComplianceFinancial(adviceOrConsultancyOnly = false, actAsIntermediary = false)
  val validCoHoProfile = CoHoCompanyProfile("status", "transactionId")

  val emptyVatScheme = VatScheme(testRegId)
  val validLodgingOfficer = VatLodgingOfficer(
    currentAddress = ScrsAddress("", ""),
    dob = validDob,
    nino = "",
    role = "director",
    name = officerName,
    changeOfName = changeOfName,
    currentOrPreviousAddress = currentOrPreviousAddress,
    contact = validOfficerContactDetails
  )
  val validSicAndCompliance = VatSicAndCompliance(
    businessDescription = testBusinessActivityDescription,
    culturalCompliance = Some(VatComplianceCultural(notForProfit = false)),
    labourCompliance = None,
    financialCompliance = None,
    mainBusinessActivity = sicCode
  )
  val validVatContact = VatContact(
    digitalContact = VatDigitalContact(email = "asd@com", tel = Some("123"), mobile = None),
    website = None,
    ppob = scrsAddress
  )
  val validVatScheme = VatScheme(
    id = testRegId,
    tradingDetails = Some(validVatTradingDetails),
    financials = Some(validVatFinancials),
    vatContact = Some(validVatContact),
    lodgingOfficer = Some(validLodgingOfficer),
    vatSicAndCompliance = Some(validSicAndCompliance),
    vatFlatRateScheme = Some(validVatFlatRateScheme)
  )
  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    id = testRegId,
    vatSicAndCompliance = Some(validSicAndCompliance),
    financials = Some(
      VatFinancials(
        bankAccount = None,
        turnoverEstimate = 0L,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = false,
        accountingPeriods = monthlyAccountingPeriod
      )
    )
  )

  val testIncorporationInfo = IncorporationInfo(
    IncorpSubscription(
      transactionId = "000-434-23",
      regime = "vat",
      subscriber = "scrs",
      callbackUrl = "http://localhost:9896/TODO-CHANGE-THIS"),
    IncorpStatusEvent(
      status = "accepted",
      crn = Some("90000001"),
      incorporationDate = Some(LocalDate.of(2016, 8, 5)),
      description = Some("Some description")))

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
                 id: String = testRegId,
                 vatTradingDetails: Option[VatTradingDetails] = None,
                 sicAndCompliance: Option[VatSicAndCompliance] = None,
                 contact: Option[VatContact] = None,
                 vatFlatRateScheme: Option[VatFlatRateScheme] = None,
                 vatEligibility: Option[VatServiceEligibility] = None
               ): VatScheme = VatScheme(
    id = id,
    tradingDetails = vatTradingDetails,
    vatSicAndCompliance = sicAndCompliance,
    vatContact = contact,
    vatFlatRateScheme = vatFlatRateScheme,
    vatServiceEligibility = vatEligibility
  )

}
