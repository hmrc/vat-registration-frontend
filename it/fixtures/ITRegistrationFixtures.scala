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
import models.api._
import models.api.returns.{JanuaryStagger, OverseasCompliance, Quarterly, Returns, StoringWithinUk}
import models.external._
import models.view._
import play.api.libs.json.Json

import java.time.LocalDate

trait ITRegistrationFixtures extends ApplicantDetailsFixture {
  val testRegId = "1"
  val testArn = "testArn"
  val testCreatedDate = LocalDate.of(2021, 1, 1)
  val tradingDetails = TradingDetails(
    tradingNameView = Some(TradingNameView(yesNo = false, tradingName = None)),
    euGoods = Some(false),
    tradeVatGoodsOutsideUk = Some(false)
  )

  val sicAndCompliance = SicAndCompliance(
    description = Some(BusinessActivityDescription("test company desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(SicCode("AB123", "super business", "super business by super people"))),
    businessActivities = Some(BusinessActivities(List(
      SicCode("AB123", "super business", "super business by super people")))
    )
  )

  val voluntaryThreshold = Threshold(
    mandatoryRegistration = false
  )

  val threshold = Threshold(
    mandatoryRegistration = true,
    thresholdPreviousThirtyDays = None,
    thresholdInTwelveMonths = Some(LocalDate.of(2018, 5, 30))
  )

  val flatRateScheme = FlatRateScheme(joinFrs = Some(false))
  val turnOverEstimates = TurnoverEstimates(turnoverEstimate = 30000)
  val testBankName = "testName"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
  val testUkBankDetails = BankAccountDetails(testBankName, testAccountNumber, testSortCode)
  val bankAccount = BankAccount(isProvided = true, Some(testUkBankDetails), None, None)
  val testBic = "BIC"
  val testIban = "IBAN"
  val testOverseasBankAccountDetails: OverseasBankDetails = OverseasBankDetails(testBankName, testBic, testIban)
  val testOverseasBankAccount: BankAccount = BankAccount(isProvided = true, None, Some(testOverseasBankAccountDetails), None)
  val returns = Returns(None, None, Some(Quarterly), Some(JanuaryStagger), None)
  val fullReturns: Returns = Returns(Some(1234), Some(true), Some(Quarterly), Some(JanuaryStagger), None, None)
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
    turnOverEstimates,
    MTDfB,
    UkCompany,
    isTransactor = false,
    registrationReason = ForwardLook,
    calculatedDate = Some(testCalculatedDate)
  )

  val testEligibilitySubmissionDataPartner: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    turnOverEstimates,
    MTDfB,
    Partnership,
    isTransactor = false,
    registrationReason = ForwardLook
  )

  val validBusinessContactDetails = BusinessContact(
    companyContactDetails = Some(CompanyContactDetails(
      email = "test@foo.com",
      phoneNumber = Some("123"),
      mobileNumber = Some("987654"),
      websiteAddress = Some("/test/url")
    )),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email)
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
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  val emptyVatSchemeNetp: VatScheme = VatScheme(
    testRegId,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP))
  )

  val emptyVatSchemeNonUkCompany: VatScheme = VatScheme(
    testRegId,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished))
  )

  val vatReg = VatScheme(
    id = "1",
    status = VatRegStatus.draft,
    tradingDetails = Some(tradingDetails),
    applicantDetails = None,
    sicAndCompliance = Some(sicAndCompliance),
    businessContact = Some(validBusinessContactDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    returns = Some(returns),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  val fullVatScheme = VatScheme(
    id = "1",
    status = VatRegStatus.draft,
    tradingDetails = Some(tradingDetails),
    applicantDetails = Some(validFullApplicantDetails),
    sicAndCompliance = Some(sicAndCompliance),
    businessContact = Some(validBusinessContactDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    returns = Some(fullReturns),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  lazy val fullNetpVatScheme: VatScheme = fullVatScheme.copy(
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP)),
    applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testNetpSoleTrader), personalDetails = Some(testNetpPersonalDetails))),
    tradingDetails = Some(tradingDetails.copy(tradingNameView = Some(TradingNameView(yesNo = true, Some(testCompanyName))), None, None, None)),
    returns = Some(fullReturns.copy(overseasCompliance = Some(testFullOverseasCompliance))),
    bankAccount = Some(testOverseasBankAccount)
  )

  val vatRegIncorporated = VatScheme(
    id = "1",
    status = VatRegStatus.draft,
    tradingDetails = Some(tradingDetails),
    applicantDetails = None,
    sicAndCompliance = Some(sicAndCompliance),
    businessContact = Some(validBusinessContactDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )
  val fullEligibilityDataJson = Json.parse(
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
                                           """.stripMargin)

  val testCrn = "testCrn"
  val testChrn = "testChrn"
  val testCasc = "testCasc"
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate = Some(LocalDate.of(2020, 2, 3))

  val testIncorpDetails = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = true, "REGISTERED", Some(BvPass), Some(testBpSafeId))

  val testSautr = "1234567890"
  val testRegistration = "REGISTERED"
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

  val completeApplicantDetails = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    hasFormerName = Some(true),
    formerName = Some(Name(Some("New"), Some("Name"),"Cosmo")),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, Some(validPrevAddress)))
  )

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
    email = Some("test@test.com"),
    emailVerified = Some(true),
    address = Some(address),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  lazy val testBusinessName = "testBusinessName"
  lazy val testVrn = "123456782"
  lazy val fullOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(true),
    vrn = Some(testVrn),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  )
}
