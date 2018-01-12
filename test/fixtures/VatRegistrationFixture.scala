/*
 * Copyright 2018 HM Revenue & Customs
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

import common.enums.VatRegStatus
import features.officer.fixtures.LodgingOfficerFixtures
import features.officer.models.view._
import features.returns.{Frequency, Returns, Start}
import features.tradingDetails.TradingDetails
import models.api._
import models.external.{IncorporationInfo, _}
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.{BankAccount, BankAccountDetails, S4LVatSicAndCompliance}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap

trait BaseFixture {
  //Test variables
  val testDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
}

trait VatRegistrationFixture extends FlatRateFixtures with TradingDetailsFixtures
  with FinancialsFixtures with LodgingOfficerFixtures {

  val sicCode = SicCode("88888888", "description", "displayDetails")

  //Responses
  val IM_A_TEAPOT = 418
  val forbidden = Upstream4xxResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val noContent = HttpResponse(204)
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val validHttpResponse = HttpResponse(OK)

  // CacheMap from S4l
  val validCacheMap = CacheMap("fooBarWizzBand",Map("foo" -> Json.toJson("wizz")))

  //Exceptions
  val badRequest = new BadRequestException(BAD_REQUEST.toString)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")
  val internalServerError = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val exception = new Exception(BAD_GATEWAY.toString)

  //Test variables
  val contextRoot = "/register-for-vat"
  val testNino: String = "AA 12 34 56 C"
  val testBusinessActivityDescription = "description"
  val testRegId = "VAT123456"
  val testMonthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")
  val testPresentationFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val validBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberWithSortCodeIsValid": true,
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val validBankCheckJsonResponse = Json.parse(validBankCheckJsonResponseString)

  val invalidBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberWithSortCodeIsValid": false,
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val invalidBankCheckJsonResponse = Json.parse(invalidBankCheckJsonResponseString)

  val s4lVatSicAndComplianceWithoutLabour = S4LVatSicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.id, Some(sicCode))),
    companyProvideWorkers = None,
    workers = None,
    temporaryContracts = None,
    skilledWorkers = None)

  val s4lVatSicAndComplianceWithLabour = S4LVatSicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.id, Some(sicCode))),
    companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
    workers = Some(Workers(20)),
    temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
    skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES)))

  //View models
  val validOfficerContactDetailsView = ContactDetailsView(Some("test@test.com"), Some("07837483287"), Some("07827483287"))
  val validCompanyProvideWorkers = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)
  val validWorkers = Workers(8)
  val validTemporaryContracts = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)
  val validSkilledWorkers = SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO)
  val validBusinessActivityDescription = BusinessActivityDescription(testBusinessActivityDescription)
  val validBusinessContactDetails = BusinessContactDetails(email = "test@foo.com", daytimePhone = Some("123"), mobile = None, website = None)

  //Api models
  val officer = Officer(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director", None, None)
  val validExpectedOverTrue = Some(VatExpectedThresholdPostIncorp(true,Some(testDate)))
  val validExpectedOverTrueNoDate = Some(VatExpectedThresholdPostIncorp(true,None))
  val validExpectedOverFalse = Some(VatExpectedThresholdPostIncorp(false,None))
  def validServiceEligibility(nes : String = VatEligibilityChoice.NECESSITY_VOLUNTARY, reason : Option[String] = None, expectedThreshold: Option[VatExpectedThresholdPostIncorp] = None) =
      VatServiceEligibility(
        Some(true),
        Some(false),
        Some(false),
        Some(false),
        Some(false),
        Some(false),
        Some(VatEligibilityChoice(
          nes, reason, None, expectedThreshold)))

  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))
  val validVatThresholdPostIncorp = VatThresholdPostIncorp(overThresholdSelection = false, None)
  val validCoHoProfile = CoHoCompanyProfile("status", "transactionId")

  val emptyVatScheme = VatScheme(testRegId, status = VatRegStatus.draft)

  val validVatContact = VatContact(
    digitalContact = VatDigitalContact(email = "asd@com", tel = Some("123"), mobile = None),
    website = None,
    ppob = scrsAddress
  )

  val validReturns = Returns(
    Some(false), Some(Frequency.monthly), None, Some(Start(Some(LocalDate.of(2017, 10, 10))))
  )

  val validBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))

  val validVatScheme = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    financials = Some(validVatFinancials),
    vatContact = Some(validVatContact),
    lodgingOfficer = Some(validFullLodgingOfficer),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    vatFlatRateScheme = Some(validVatFlatRateScheme),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft
  )

  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    status = VatRegStatus.draft,
    id = testRegId,
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    lodgingOfficer = Some(emptyLodgingOfficer),
    financials = Some(
      VatFinancials(
        zeroRatedTurnoverEstimate = None
      )
    ),
    bankAccount = Some(validBankAccount)
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

  def vatScheme(
                 id: String = testRegId,
                 tradingDetails: Option[TradingDetails] = None,
                 sicAndComp: Option[S4LVatSicAndCompliance] = None,
                 contact: Option[VatContact] = None,
                 vatFlatRateScheme: Option[VatFlatRateScheme] = None,
                 vatEligibility: Option[VatServiceEligibility] = None
               ): VatScheme = VatScheme(
    id = id,
    tradingDetails = tradingDetails,
    sicAndCompliance = sicAndComp,
    vatContact = contact,
    vatFlatRateScheme = vatFlatRateScheme,
    vatServiceEligibility = vatEligibility,
    status = VatRegStatus.draft
  )

}
