/*
 * Copyright 2021 HM Revenue & Customs
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

import common.enums.VatRegStatus
import models._
import models.api._
import models.api.returns.{Monthly, Returns}
import models.view.{Summary, SummaryRow, SummarySection}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait BaseFixture {
  //Test variables
  val testDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
  val validExpectedOverTrue = Some(testDate)

  def generateThreshold(mandatory: Boolean = false,
                        thresholdPreviousThirtyDays: Option[LocalDate] = None,
                        thresholdInTwelveMonths: Option[LocalDate] = None) =
    (mandatory, thresholdPreviousThirtyDays, thresholdInTwelveMonths) match {
      case (false, None, None) => Threshold(false)
      case (_, ptd, itm) if List(ptd, itm).flatten.nonEmpty => Threshold(true, ptd, itm)
      case _ => Threshold(false)
    }

  val validVoluntaryRegistration = generateThreshold()
  val validMandatoryRegistrationThirtyDays = generateThreshold(thresholdPreviousThirtyDays = Some(testDate))
  val validMandatoryRegistrationBothDates = generateThreshold(thresholdPreviousThirtyDays = Some(testDate), thresholdInTwelveMonths = Some(testDate))
  val validMandatoryRegistrationTwelve = generateThreshold(thresholdInTwelveMonths = Some(testDate))
  val optVoluntaryRegistration = Some(validVoluntaryRegistration)
  val optMandatoryRegistrationThirtyDays = Some(validMandatoryRegistrationThirtyDays)
  val optMandatoryRegistrationBothDates = Some(validMandatoryRegistrationBothDates)
  val optMandatoryRegistrationTwelve = Some(validMandatoryRegistrationTwelve)
}

trait VatRegistrationFixture extends FlatRateFixtures with TradingDetailsFixtures
  with ApplicantDetailsFixtures with ReturnsFixture {

  val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("accountName", "SortCode", "AccountNumber")), None)

  val sicCode = SicCode("88888", "description", "displayDetails")

  val currentThreshold = "50000"
  val taxableThreshold = TaxableThreshold(currentThreshold, "2018-1-1")
  val formattedThreshold = "50,000"

  //Responses
  val forbidden = Upstream4xxResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val noContent = HttpResponse(204, "")
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val validHttpResponse = HttpResponse(OK, "{}")

  // CacheMap from S4l
  val validCacheMap = CacheMap("fooBarWizzBand", Map("foo" -> Json.toJson("wizz")))

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
  val testHonestyDeclaration = true
  val testMonthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")
  val testPresentationFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val validBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberWithSortCodeIsValid": "yes",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val validBankCheckJsonResponse = Json.parse(validBankCheckJsonResponseString)

  val invalidBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberWithSortCodeIsValid": "no",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val invalidBankCheckJsonResponse = Json.parse(invalidBankCheckJsonResponseString)

  val indeterminateBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberWithSortCodeIsValid": "indeterminate",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val indeterminateBankCheckJsonResponse = Json.parse(indeterminateBankCheckJsonResponseString)


  val s4lVatSicAndComplianceWithoutLabour = SicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
    supplyWorkers = None,
    workers = None,
    intermediarySupply = None)

  val s4lVatSicAndComplianceWithLabour = SicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
    supplyWorkers = Some(SupplyWorkers(true)),
    workers = Some(Workers(12)),
    intermediarySupply = Some(IntermediarySupply(true))
  )

  val s4lVatSicAndComplianceWithoutDescription = SicAndCompliance(
    description = None,
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
    supplyWorkers = None,
    workers = None,
    intermediarySupply = None
  )

  //Api models
  val testCountry = Country(Some("UK"), Some("United Kingdom"))
  val testAddress = Address("line1", "line2", None, None, Some("XX XX"), Some(testCountry), addressValidated = true)

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
    Some(10000.5), Some(false), Some(Monthly), None, Some(LocalDate.of(2017, 10, 10))
  )

  val validBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")), None)

  val validBusinessContactDetails = BusinessContact(
    companyContactDetails = Some(CompanyContactDetails(
      email = "test@foo.com",
      phoneNumber = Some("123"),
      mobileNumber = Some("987654"),
      websiteAddress = Some("/test/url")
    )),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email)
  )

  val validTurnoverEstimates: TurnoverEstimates = TurnoverEstimates(100L)

  val validEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    validTurnoverEstimates,
    MTDfB,
    UkCompany
  )

  val validSoleTraderEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    validTurnoverEstimates,
    MTDfB,
    Individual
  )

  val validVatScheme = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validSoleTraderVatScheme = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validSoleTraderEligibilitySubmissionData)
  )

  val validVatSchemeNoBank = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = None,
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validVatSchemeNoTradingDetails = VatScheme(
    id = testRegId,
    tradingDetails = None,
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validVatSchemeEmptySicAndCompliance = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithoutDescription),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validVatSchemeWithLabour = VatScheme(
    id = testRegId,
    tradingDetails = Some(generateTradingDetails()),
    businessContact = Some(validBusinessContactDetails),
    applicantDetails = Some(completeApplicantDetails),
    sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validBankAccount),
    returns = Some(validReturns),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
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

  val iclMultipleResultsSicCode1 = SicCode("12345", "Whale farming", "")
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

  implicit def urlCreator: String => Call = (s: String) => Call("GET", s"http://vatRegEFEUrl/question?pageId=$s")

  val fullEligibilityDataJson = Json.parse(
    """
      |{ "sections": [
      |            {
      |              "title": "section_1",
      |              "data": [
      |                {"questionId": "mandatoryRegistration", "question": "Question 1", "answer": "FOO", "answerValue": true},
      |                {"questionId": "voluntaryRegistration", "question": "Question 2", "answer": "BAR", "answerValue": false},
      |                {"questionId": "thresholdPreviousThirtyDays", "question": "Question 3", "answer": "wizz", "answerValue": "2017-5-23"},
      |                {"questionId": "thresholdInTwelveMonths", "question": "Question 4", "answer": "woosh", "answerValue": "2017-7-16"}
      |              ]
      |            },
      |            {
      |              "title": "section_2",
      |              "data": [
      |                {"questionId": "applicantUKNino", "question": "Question 5", "answer": "bang", "answerValue": "SR123456C"},
      |                {"questionId": "turnoverEstimate", "question": "Question 6", "answer": "BUZZ", "answerValue": 2024},
      |                {"questionId": "completionCapacity", "question": "Question 7", "answer": "cablam", "answerValue": "noneOfThese"},
      |                {"questionId": "completionCapacityFillingInFor", "question": "Question 8", "answer": "weez", "answerValue": {
      |                "name": {
      |                    "first": "This is my first",
      |                    "middle": "This is my middle name",
      |                    "surname": "This is my surname"
      |                    },
      |                "role": "director"
      |                 }
      |                }
      |              ]
      |            }
      |          ]
      |         }
                                           """.stripMargin)

  val section1 = SummarySection("section_1",
    Seq(
      (SummaryRow("Question 1", Seq("FOO"), Some(urlCreator("mandatoryRegistration"))), true),
      (SummaryRow("Question 2", Seq("BAR"), Some(urlCreator("voluntaryRegistration"))), true),
      (SummaryRow("Question 3", Seq("wizz"), Some(urlCreator("thresholdPreviousThirtyDays"))), true),
      (SummaryRow("Question 4", Seq("woosh"), Some(urlCreator("thresholdInTwelveMonths"))), true)
    ), true)

  val section2 = SummarySection("section_2",
    Seq(
      (SummaryRow("Question 5", Seq("bang"), Some(urlCreator("applicantUKNino"))), true),
      (SummaryRow("Question 6", Seq("BUZZ"), Some(urlCreator("turnoverEstimate"))), true),
      (SummaryRow("Question 7", Seq("cablam"), Some(urlCreator("completionCapacity"))), true),
      (SummaryRow("Question 8", Seq("weez"), Some(urlCreator("completionCapacityFillingInFor"))), true)
    ), true)

  val fullSummaryModelFromFullEligiblityJson = Summary(section1 :: section2 :: Nil)
}