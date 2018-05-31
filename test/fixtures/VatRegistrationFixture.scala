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
import features.bankAccountDetails.models.{BankAccount, BankAccountDetails}
import features.businessContact.models.{BusinessContact, CompanyContactDetails}
import features.officer.fixtures.LodgingOfficerFixtures
import features.officer.models.view._
import features.returns.ReturnsFixture
import features.returns.models.{Frequency, Returns, Start}
import features.sicAndCompliance.models.{SicAndCompliance, _}
import features.tradingDetails.TradingDetails
import features.turnoverEstimates.TurnoverEstimates
import frs.FlatRateScheme
import models.TaxableThreshold
import models.api._
import models.external.{IncorporationInfo, _}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap

trait BaseFixture {
  //Test variables
  val testDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
  val validExpectedOverTrue = Some(testDate)
  def generateThreshold(reason: Option[String] = None,
                        overThreshold: Option[LocalDate] = None,
                        expectedOverThreshold: Option[LocalDate] = None,
                        overThresholdTwelve: Option[LocalDate] = None) =
    (reason, overThreshold, expectedOverThreshold, overThresholdTwelve) match {
      case (r@Some(_), _, _, _)                                    => Threshold(false,r)
      case (None, o, eo, ott) if List(o, eo, ott).flatten.nonEmpty => Threshold(true, None, o, eo, ott)
      case _                                                       => Threshold(false)
    }
  def generateOptionalThreshold(reason: Option[String] = None, overThreshold: Option[LocalDate] = None, expectedOverThreshold: Option[LocalDate] = None) = {
    Some(generateThreshold(reason, overThreshold, expectedOverThreshold))
  }
  val validVoluntaryRegistration            = generateThreshold()
  val validVoluntaryRegistrationWithReason  = generateThreshold(reason = Some(Threshold.INTENDS_TO_SELL))
  val validMandatoryRegistration            = generateThreshold(overThreshold = Some(testDate))
  val validMandatoryRegistrationBothDates   = generateThreshold(overThreshold = Some(testDate), expectedOverThreshold = Some(testDate))
  val validMandatoryRegistrationTwelve      = generateThreshold(overThresholdTwelve = Some(testDate))
  val optVoluntaryRegistration              = Some(validVoluntaryRegistration)
  val optVoluntaryRegistrationWithReason    = Some(validVoluntaryRegistrationWithReason)
  val optMandatoryRegistration              = Some(validMandatoryRegistration)
  val optMandatoryRegistrationBothDates     = Some(validMandatoryRegistrationBothDates)
  val optMandatoryRegistrationTwelve        = Some(validMandatoryRegistrationTwelve)
}

trait VatRegistrationFixture extends FlatRateFixtures with TradingDetailsFixtures
  with LodgingOfficerFixtures with ReturnsFixture {

  val bankAccount       = BankAccount(isProvided = true, Some(BankAccountDetails("accountName", "SortCode", "AccountNumber")))

  val sicCode = SicCode("88888", "description", "displayDetails")

  val currentThreshold = "50000"
  val taxableThreshold = TaxableThreshold(currentThreshold, "2018-1-1")
  val formattedThreshold = "50,000"

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


  val s4lVatSicAndComplianceWithoutLabour = SicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
    companyProvideWorkers = None,
    workers = None,
    temporaryContracts = None,
    skilledWorkers = None)

  val s4lVatSicAndComplianceWithLabour = SicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
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

  //Api models
  val officer = Officer(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director", None, None)

  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))

  val validCoHoProfile = CoHoCompanyProfile("status", "transactionId")

  val validCompanyRegistrationProfile = Some(CompanyRegistrationProfile("submitted", Some("04")))

  val validCompRegProfileJson: JsObject = Json.parse(
    """
      |{
      |  "status":"submitted",
      |  "acknowledgementReferences":{
      |    "status":"04"
      |  }
      |}
    """.stripMargin).as[JsObject]

  val emptyVatScheme = VatScheme(testRegId, status = VatRegStatus.draft)

  val validReturns = Returns(
    Some(false), Some(Frequency.monthly), None, Some(Start(Some(LocalDate.of(2017, 10, 10))))
  )

  val validBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))

  val validBusinessContactDetails = BusinessContact(
    companyContactDetails = Some(CompanyContactDetails(
      email          = "test@foo.com",
      phoneNumber    = Some("123"),
      mobileNumber   = Some("987654"),
      websiteAddress = Some("/test/url")
    )),
    ppobAddress = Some(scrsAddress)
  )

  val validVatScheme = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    lodgingOfficer = Some(validFullLodgingOfficer),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    turnOverEstimates = Some(TurnoverEstimates(100L))
  )

  val emptyVatSchemeWithAccountingPeriodFrequency = VatScheme(
    status = VatRegStatus.draft,
    id = testRegId,
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    lodgingOfficer = Some(emptyLodgingOfficer),
    bankAccount = Some(validBankAccount),
    turnOverEstimates = Some(TurnoverEstimates(100L))
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
                 sicAndComp: Option[SicAndCompliance] = None,
                 businessContact: Option[BusinessContact] = None,
                 flatRateScheme: Option[FlatRateScheme] = None,
                 threshold: Option[Threshold] = None
               ): VatScheme = VatScheme(
    id = id,
    tradingDetails = tradingDetails,
    sicAndCompliance = sicAndComp,
    businessContact = businessContact,
    flatRateScheme = flatRateScheme,
    threshold = threshold,
    status = VatRegStatus.draft
  )

  // ICL
  val iclMultipleResults = Json.parse(
    """
      | {
      |   "sicCodes": [
      |     {
      |       "code" : "12345",
      |       "desc" : "Whale farming",
      |       "indexes" : [
      |         {
      |         "desc" : "Fish farming",
      |         "desc" : "Fish-related activities"
      |         } ]
      |        },
      |     {
      |       "code" : "23456",
      |       "desc" : "Dog walking",
      |       "indexes" : [
      |         {
      |         "desc" : "Animal services"
      |         } ]
      |        }
      |   ]
      | }
    """.stripMargin).as[JsObject]

  val iclMultipleResultsSicCode1 = SicCode("12345","Whale farming", "")
  val iclMultipleResultsSicCode2 = SicCode("23456", "Dog walking", "")

  val iclSingleResult = Json.parse(
    """
      | {
      |   "sicCodes": [
      |     {
      |       "code" : "12345",
      |       "desc" : "Whale farming",
      |       "indexes" : [
      |         {
      |         "desc" : "Fish farming",
      |         "desc" : "Fish-related activities"
      |         } ]
      |        }
      |       ]
      |     }
    """.stripMargin).as[JsObject]

}
