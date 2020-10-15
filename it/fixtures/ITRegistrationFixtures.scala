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

import java.time.LocalDate

import common.enums.VatRegStatus
import fixtures.ApplicantDetailsFixture
import models._
import models.api._
import models.external.{CoHoRegisteredOfficeAddress, EmailAddress, EmailVerified}
import models.view._
import play.api.libs.json.Json

trait ITRegistrationFixtures extends ApplicantDetailsFixture {
  val address = ScrsAddress(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"))


  val tradingDetails = TradingDetails(
    tradingNameView = Some(TradingNameView(yesNo = false, tradingName = None)),
    euGoods = Some(false)
  )

  val sicAndCompliance = SicAndCompliance(
    description = Some(BusinessActivityDescription("test company desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(SicCode("AB123", "super business", "super business by super people"))),
    otherBusinessActivities = Some(OtherBusinessActivities(List(
      SicCode("AB123", "super business", "super business by super people")))
    )
  )


  val voluntaryThreshold = Threshold(
    mandatoryRegistration = false
  )

  val threshold = Threshold(
    mandatoryRegistration = true,
    thresholdPreviousThirtyDays = Some(LocalDate.of(2016, 9, 30)),
    thresholdInTwelveMonths = Some(LocalDate.of(2017, 1, 1))
  )

  val flatRateScheme = FlatRateScheme(joinFrs = Some(false))

  val turnOverEstimates = TurnoverEstimates(turnoverEstimate = 30000)
  val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))


  val returns = Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None)

  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))

  val testEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    turnOverEstimates,
    MTDfB
  )

  val coHoRegisteredOfficeAddress =
    CoHoRegisteredOfficeAddress("premises",
      "line1",
      Some("line2"),
      "locality",
      Some("UK"),
      Some("po_box"),
      Some("XX XX"),
      Some("region"))

  val validBusinessContactDetails = BusinessContact(
    companyContactDetails = Some(CompanyContactDetails(
      email = "test@foo.com",
      phoneNumber = Some("123"),
      mobileNumber = Some("987654"),
      websiteAddress = Some("/test/url")
    )),
    ppobAddress = Some(scrsAddress),
    contactPreference = Some(Email)
  )

  val validBusinessContactDetailsJson = Json.parse(
    """
      |{
      |"ppob" : {
      |   "line1"    : "line1",
      |   "line2"    : "line2",
      |   "postcode" : "XX XX",
      |   "country"  : "UK"
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

  val testIncorpDetails = IncorporationDetails(testCrn, testCompanyName, testCtUtr, testIncorpDate)

  val completeApplicantDetails = ApplicantDetails(
    incorporationDetails = Some(testIncorpDetails),
    transactorDetails = Some(testTransactorDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, Some(validPrevAddress)))
  )

}
