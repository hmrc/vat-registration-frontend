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
import features.bankAccountDetails.models.{BankAccount, BankAccountDetails}
import features.businessContact.models.{BusinessContact, CompanyContactDetails}
import features.officer.fixtures.LodgingOfficerFixture
import features.returns.models.{Frequency, Returns, Stagger}
import features.sicAndCompliance.models.{BusinessActivityDescription, MainBusinessActivityView, OtherBusinessActivities, SicAndCompliance}
import features.tradingDetails.{TradingDetails, TradingNameView}
import features.turnoverEstimates.TurnoverEstimates
import frs.FlatRateScheme
import models.api._
import models.external.CoHoRegisteredOfficeAddress
import play.api.libs.json.Json

trait ITRegistrationFixtures extends LodgingOfficerFixture {
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
    mandatoryRegistration = false,
    voluntaryReason       = Some(Threshold.SELLS)
  )

  val threshold = Threshold(
    mandatoryRegistration     = true,
    voluntaryReason           = None,
    overThresholdDateThirtyDays         = Some(LocalDate.of(2016, 9, 30)),
    pastOverThresholdDateThirtyDays = Some(LocalDate.of(2016, 9, 30)),
    overThresholdOccuredTwelveMonth = Some(LocalDate.of(2017, 1, 1))
  )

  val flatRateScheme     = FlatRateScheme(joinFrs = Some(false))

  val turnOverEstimates = TurnoverEstimates(vatTaxable = 30000)
  val bankAccount     = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))


  val returns         = Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None)

  val scrsAddress = ScrsAddress("line1", "line2", None, None, Some("XX XX"), Some("UK"))

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
      email          = "test@foo.com",
      phoneNumber    = Some("123"),
      mobileNumber   = Some("987654"),
      websiteAddress = Some("/test/url")
    )),
    ppobAddress = Some(scrsAddress)
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
      | "website"   :"/test/url"
      |
      |}
    """.stripMargin
  )


  val vatReg = VatScheme(
    id                  = "1",
    status              = VatRegStatus.draft,
    tradingDetails      = Some(tradingDetails),
    lodgingOfficer      = None,
    sicAndCompliance    = Some(sicAndCompliance),
    businessContact     = Some(validBusinessContactDetails),
    threshold           = Some(voluntaryThreshold),
    flatRateScheme      = Some(flatRateScheme),
    turnOverEstimates   = Some(turnOverEstimates),
    bankAccount         = Some(bankAccount),
    returns             = Some(returns)
  )

  val vatRegIncorporated = VatScheme(
    id                  = "1",
    status              = VatRegStatus.draft,
    tradingDetails      = Some(tradingDetails),
    lodgingOfficer      = None,
    sicAndCompliance    = Some(sicAndCompliance),
    businessContact     = Some(validBusinessContactDetails),
    threshold           = Some(threshold),
    flatRateScheme      = Some(flatRateScheme),
    bankAccount         = Some(bankAccount)
  )

}
