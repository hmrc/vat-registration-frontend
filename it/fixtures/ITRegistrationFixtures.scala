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

package it.fixtures

import common.enums.VatRegStatus
import fixtures.ApplicantDetailsFixture
import models._
import models.api._
import models.api.returns.{JanuaryStagger, Quarterly, Returns}
import models.external.{BvPass, EmailAddress, EmailVerified, LimitedCompany}
import models.view._
import play.api.libs.json.Json

import java.time.LocalDate

trait ITRegistrationFixtures extends ApplicantDetailsFixture {
  val testRegId = "1"
  val address = Address(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"), addressValidated = true)

  val tradingDetails = TradingDetails(
    tradingNameView = Some(TradingNameView(yesNo = false, tradingName = None)),
    euGoods = Some(false)
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
  val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")), None)
  val returns = Returns(None, None, Some(Quarterly), Some(JanuaryStagger), None)
  val fullReturns = Returns(Some(1234), Some(true), Some(Quarterly), Some(JanuaryStagger), None)
  val testCountry = Country(Some("UK"), Some("United Kingdom"))
  val addressWithCountry = Address("line1", "line2", None, None, Some("XX XX"), Some(testCountry), addressValidated = true)

  val testEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    turnOverEstimates,
    MTDfB,
    UkCompany
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
    applicantDetails = None,
    sicAndCompliance = Some(sicAndCompliance),
    businessContact = Some(validBusinessContactDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    returns = Some(fullReturns),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
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
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate = LocalDate.of(2020, 2, 3)

  val testIncorpDetails = LimitedCompany(testCrn, testCompanyName, testCtUtr, testIncorpDate, "GB", identifiersMatch = true, Some("REGISTERED"), Some(BvPass), Some(testBpSafeId))

  val completeApplicantDetails = ApplicantDetails(
    entity = Some(testIncorpDetails),
    transactor = Some(testTransactorDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, Some(validPrevAddress)))
  )

}
