/*
 * Copyright 2022 HM Revenue & Customs
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
import models.api.vatapplication._
import models.external.{BvPass, PartnershipIdEntity}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

trait BaseFixture {
  //Test variables
  val testDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
  val validLabourSicCode: SicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance: SicCode = SicCode("12345678", "fooBar", "FooBar")
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

trait VatRegistrationFixture extends BaseFixture with FlatRateFixtures with ApplicantDetailsFixtures {

  val ukBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("accountName", "SortCode", "AccountNumber")), None)

  val sicCode = SicCode("88888", "description", "displayDetails")

  val currentThreshold = "50000"
  val formattedThreshold = "50,000"

  //Responses
  val forbidden = UpstreamErrorResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val noContent = HttpResponse(204, "")
  val upstream4xx = UpstreamErrorResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = UpstreamErrorResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val validHttpResponse = HttpResponse(OK, "{}")

  val validCacheMap = CacheMap("fooBarWizzBand", Map("foo" -> Json.toJson("wizz")))

  //Exceptions
  val badRequest = new BadRequestException(BAD_REQUEST.toString)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")
  val internalServerError = UpstreamErrorResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val exception = new Exception(BAD_GATEWAY.toString)

  //Test variables
  val contextRoot = "/register-for-vat"
  val testNino: String = "AA 12 34 56 C"
  val testBusinessActivityDescription = "description"
  val testRegId = "VAT123456"
  val testHonestyDeclaration = true
  val validBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "yes",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val validBankCheckJsonResponse = Json.parse(validBankCheckJsonResponseString)

  val invalidBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "no",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val invalidBankCheckJsonResponse = Json.parse(invalidBankCheckJsonResponseString)

  val indeterminateBankCheckJsonResponseString =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "indeterminate",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val indeterminateBankCheckJsonResponse = Json.parse(indeterminateBankCheckJsonResponseString)

  val complianceWithoutLabour = LabourCompliance(None, None, None)
  val complianceWithLabour = LabourCompliance(Some(12), Some(true), Some(true))

  lazy val validTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(PersonalDetails(
      firstName = "testFirstName",
      lastName = "testLastName",
      nino = Some("AB123456C"),
      trn = None,
      identifiersMatch = true,
      dateOfBirth = Some(LocalDate.of(2020, 1, 1))
    )),
    isPartOfOrganisation = Some(true),
    organisationName = Some("testCompanyName"),
    telephone = Some("1234"),
    email = Some("test@t.test"),
    emailVerified = Some(true),
    address = Some(testAddress),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  //Api models

  val testLine1 = "line1"
  val testLine2 = "line2"
  val testLine3 = "line3"
  val testLine4 = "line4"
  val testLine5 = "line5"
  val testCountry = Country(Some("UK"), Some("United Kingdom"))
  val testAddress = Address(testLine1, Some(testLine2), None, None, None, Some(testPostcode), Some(testCountry), addressValidated = true)

  val validCompRegProfileJson: JsObject = Json.parse(
    """
      |{
      |  "status":"submitted",
      |  "acknowledgementReferences":{
      |    "status":"04"
      |  }
      |}
    """.stripMargin).as[JsObject]

  val emptyVatScheme = VatScheme(testRegId, testDate, status = VatRegStatus.draft)

  val validPartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    companyName = Some(testCompanyName),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    identifiersMatch = true
  )

  val validEntities = List[Entity](
    Entity(
      details = Some(validPartnershipIdEntity),
      partyType = LtdLiabilityPartnership,
      isLeadPartner = Some(true),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    ),
    Entity(
      details = Some(validPartnershipIdEntity),
      partyType = LtdLiabilityPartnership,
      isLeadPartner = Some(false),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    )
  )

  val validNipCompliance = NIPTurnover(
    goodsToEU = Some(ConditionalValue(true, Some(BigDecimal(1)))),
    goodsFromEU = Some(ConditionalValue(true, Some(BigDecimal(1))))
  )

  val testTurnover = 100
  val testZeroRatedSupplies = 10000.5
  val validVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(testZeroRatedSupplies),
    claimVatRefunds = Some(false),
    appliedForExemption = None,
    startDate = Some(LocalDate.of(2017, 10, 10)),
    returnsFrequency = Some(Monthly),
    staggerStart = Some(MonthlyStagger)
  )

  val validAasDetails = AASDetails(Some(QuarterlyPayment), Some(BACS))

  val validUkBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")), None)

  val noUkBankAccount = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))

  val validEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    UkCompany,
    isTransactor = false,
    appliedForException = None,
    registrationReason = ForwardLook
  )

  val validSoleTraderEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    Individual,
    isTransactor = false,
    appliedForException = None,
    registrationReason = ForwardLook
  )

  val validBusiness = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    labourCompliance = Some(complianceWithoutLabour),
    businessDescription = Some(testBusinessActivityDescription),
    mainBusinessActivity = Some(sicCode)
  )

  val validBusinessWithNoDescription = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    labourCompliance = Some(complianceWithoutLabour)
  )

  val validBusinessWithNoDescriptionAndLabour = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    mainBusinessActivity = Some(sicCode),
    businessActivities = Some(List(sicCode))
  )

  val otherBusinessInvolvementWithVrn = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(true),
    vrn = Some("123456782"),
    hasUtr = None,
    utr = None,
    stillTrading = Some(false))
  val otherBusinessInvolvementWithUtr = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(true),
    utr = Some("123456782"),
    stillTrading = Some(false))
  val otherBusinessInvolvementWithoutVrnUtr = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(false),
    utr = None,
    stillTrading = Some(false))
  val otherBusinessInvolvementWithPartialData = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = None,
    vrn = None,
    hasUtr = None,
    utr = None,
    stillTrading = None)

  val validVatScheme = VatScheme(
    registrationId = testRegId,
    createdDate = testDate,
    business = Some(validBusiness),
    applicantDetails = Some(completeApplicantDetails),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validUkBankAccount),
    vatApplication = Some(validVatApplication),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validSoleTraderVatScheme = VatScheme(
    registrationId = testRegId,
    createdDate = testDate,
    business = Some(validBusiness),
    applicantDetails = Some(completeApplicantDetails),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validUkBankAccount),
    vatApplication = Some(validVatApplication),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validSoleTraderEligibilitySubmissionData)
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

  val fullEligibilityDataJson = Json.parse(
    """
      |{ "sections": [
      |            {
      |              "title": "section_1",
      |              "data": [
      |                {"questionId": "mandatoryRegistration", "question": "Question 1", "answer": "FOO", "answerValue": true},
      |                {"questionId": "voluntaryRegistration", "question": "Question 2", "answer": "BAR", "answerValue": false},
      |                {"questionId": "thresholdPreviousThirtyDays", "question": "Question 3", "answer": "wizz", "answerValue": "2017-05-23"},
      |                {"questionId": "thresholdInTwelveMonths", "question": "Question 4", "answer": "woosh", "answerValue": "2017-07-16"}
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

}