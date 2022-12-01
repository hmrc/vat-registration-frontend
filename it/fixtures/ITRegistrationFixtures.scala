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

import common.enums.VatRegStatus
import models._
import models.api.SicCode.SIC_CODES_KEY
import models.api._
import models.api.vatapplication._
import models.external._
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import java.time.LocalDate

trait ITRegistrationFixtures extends ApplicantDetailsFixture {
  val testRegId = "1"
  val testArn = "testArn"
  val testCreatedDate = LocalDate.of(2021, 1, 1)

  val voluntaryThreshold = Threshold(
    mandatoryRegistration = false
  )

  val threshold = Threshold(
    mandatoryRegistration = true,
    thresholdPreviousThirtyDays = None,
    thresholdInTwelveMonths = Some(LocalDate.of(2018, 5, 30))
  )

  val flatRateScheme = FlatRateScheme(joinFrs = Some(false))
  val testBankName = "testName"
  val testSortCode = "123456"
  val testAccountNumber = "12345678"
  val testUkBankDetails = BankAccountDetails(testBankName, testAccountNumber, testSortCode, Some(ValidStatus))
  val bankAccount = BankAccount(isProvided = true, Some(testUkBankDetails), None)
  val emptyBankAccount = BankAccount(isProvided = true, None, None)
  val bankAccountNotProvidedNoReason = BankAccount(isProvided = false, None, None)
  val bankAccountNotProvided = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))
  val testTurnover = 30000
  val fullVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(1234),
    northernIrelandProtocol = None,
    claimVatRefunds = Some(true),
    appliedForExemption = None,
    overseasCompliance = None,
    startDate = None,
    returnsFrequency = Some(Quarterly),
    staggerStart = Some(JanuaryStagger),
    annualAccountingDetails = None,
    hasTaxRepresentative = None
  )
  val testCalculatedDate: LocalDate = LocalDate.now()
  val testLine1 = "line1"
  val testLine2 = "line2"
  val testCountry = Country(Some("UK"), Some("United Kingdom"))
  val address = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE1 1ST"), addressValidated = true)
  val addressWithCountry = Address(testLine1, Some(testLine2), None, None, None, Some("XX XX"), Some(testCountry), addressValidated = true)

  val testWarehouseNumber = "tst123456789012"
  val testWarehouseName = "testWarehouseName"
  val testFullOverseasCompliance: OverseasCompliance = OverseasCompliance(
    goodsToOverseas = Some(true),
    goodsToEu = Some(true),
    storingGoodsForDispatch = Some(StoringWithinUk),
    usingWarehouse = Some(true),
    fulfilmentWarehouseNumber = Some(testWarehouseNumber),
    fulfilmentWarehouseName = Some(testWarehouseName)
  )

  val testEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    UkCompany,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook,
    calculatedDate = Some(testCalculatedDate)
  )

  val testEligibilitySubmissionDataPartner: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    Partnership,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook
  )

  val businessDetails: Business = Business(
    hasTradingName = Some(true),
    email = Some("test@foo.com"),
    telephoneNumber = Some("987654"),
    website = Some("/test/url"),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email),
    welshLanguage = Some(false),
    businessDescription = Some("test company desc"),
    mainBusinessActivity = Some(SicCode("AB123", "super business", "super business by super people")),
    businessActivities = Some(List(SicCode("AB123", "super business", "super business by super people")))
  )

  val validBusinessContactDetailsJson = Json.parse(
    """
      |{
      |"ppob" : {
      |   "line1"    : "line1",
      |   "line2"    : "line2",
      |   "postcode" : "XX XX",
      |   "country"  : {
      |     "code": "UK",
      |     "name": "United Kingdom"
      |   },
      |   "addressValidated" : true
      | },
      | "digitalContact" : {
      |   "email"    : "test@foo.com",
      |   "tel"      : "123",
      |   "mobile"   : "987654"
      | },
      | "website"   :"/test/url",
      | "contactPreference": "Email"
      |}
    """.stripMargin
  )

  val emptyUkCompanyVatScheme: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  val emptyVatSchemeNetp: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP))
  )

  val emptyVatSchemeNonUkCompany: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished))
  )

  val fullVatScheme = VatScheme(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    applicantDetails = Some(validFullApplicantDetails),
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    vatApplication = Some(fullVatApplication),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  lazy val fullNetpVatScheme: VatScheme = fullVatScheme.copy(
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP)),
    applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testNetpSoleTrader), personalDetails = Some(testNetpPersonalDetails))),
    business = Some(businessDetails.copy(tradingName = Some(testCompanyName))),
    vatApplication = Some(fullVatApplication.copy(overseasCompliance = Some(testFullOverseasCompliance))),
    bankAccount = None
  )

  val vatRegIncorporated = VatScheme(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    applicantDetails = None,
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )
  val fullEligibilityDataJson: JsObject = Json.parse(
    """
      |{ "sections": [
      |            {
      |              "title": "section A",
      |              "data": [
      |                {"questionId": "mandatoryRegistration", "question": "Question 1", "answer": "FOO", "answerValue": true},
      |                {"questionId": "voluntaryRegistration", "question": "Question 2", "answer": "BAR", "answerValue": false},
      |                {"questionId": "thresholdPreviousThirtyDays", "question": "Question 3", "answer": "wizz", "answerValue": "2017-5-23"},
      |                {"questionId": "thresholdInTwelveMonths", "question": "Question 4", "answer": "woosh", "answerValue": "2017-7-16"}
      |              ]
      |            },
      |            {
      |              "title": "section B",
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
    """.stripMargin).as[JsObject]

  val testCrn = "testCrn"
  val testChrn = "testChrn"
  val testCasc = "testCasc"
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate = Some(LocalDate.of(2020, 2, 3))

  val testIncorpDetails = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = true, RegisteredStatus, Some(BvPass), Some(testBpSafeId))

  val testSautr = "1234567890"
  val testRegistration = RegisteredStatus
  val testSafeId = "X00000123456789"
  val testSoleTrader: SoleTraderIdEntity = SoleTraderIdEntity(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = Some(testApplicantNino),
    sautr = Some(testSautr),
    trn = None,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testNetpSoleTrader: SoleTraderIdEntity = SoleTraderIdEntity(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = None,
    sautr = Some(testSautr),
    trn = Some(testTrn),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = false
  )

  lazy val validTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(testPersonalDetails),
    isPartOfOrganisation = Some(true),
    organisationName = Some("testCompanyName"),
    telephone = Some("1234"),
    email = Some("test@test.com"),
    emailVerified = Some(true),
    address = Some(address),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  lazy val testBusinessName = "testBusinessName"
  lazy val testHasVrn = true
  lazy val testVrn = "123456782"
  lazy val fullOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(testHasVrn),
    vrn = Some(testVrn),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  )

  val sicCodeId = "81300003"
  val sicCodeDesc = "test2 desc"
  val sicCodeDescCy = "test2 desc cy"
  val businessActivityDescription = "test business desc"

  val jsonListSicCode =
    s"""
       |  [
       |    {
       |      "code": "01110004",
       |      "desc": "gdfgdg d",
       |      "descCy": "gdfgdg d cy"
       |    },
       |    {
       |      "code": "$sicCodeId",
       |      "desc": "$sicCodeDesc",
       |      "descCy": "$sicCodeDescCy"
       |    },
       |    {
       |      "code": "82190004",
       |      "desc": "ry rty try rty ",
       |      "descCy": "ry rty try rty "
       |    }
       |  ]
        """.stripMargin

  val sicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(models.CurrentProfile("1", VatRegStatus.draft)),
    SIC_CODES_KEY -> Json.parse(jsonListSicCode)
  )

  val iclSicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(models.CurrentProfile("1", VatRegStatus.draft)),
    "ICLFetchResultsUri" -> JsString("/fetch-results")
  )

  val mainBusinessActivity = SicCode(sicCodeId, sicCodeDesc, sicCodeDescCy)

  val fullModel = Business(
    email = Some("test@foo.com"),
    telephoneNumber = Some("987654"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email),
    welshLanguage = Some(false),
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity),
    businessActivities = Some(List(mainBusinessActivity)),
    labourCompliance = Some(LabourCompliance(
      supplyWorkers = Some(true),
      numOfWorkersSupplied = Some(200),
      intermediaryArrangement = Some(true),
    ))
  )

  val modelWithoutCompliance = Business(
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity)
  )

}
