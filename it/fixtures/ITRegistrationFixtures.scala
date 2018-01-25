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
import features.officer.fixtures.LodgingOfficerFixture
import features.returns.{Frequency, Returns, Stagger}
import features.sicAndCompliance.models.{BusinessActivityDescription, MainBusinessActivityView, SicAndCompliance}
import features.tradingDetails.{TradingDetails, TradingNameView}
import features.turnoverEstimates.TurnoverEstimates
import frs.FlatRateScheme
import models.api._
import models.{BankAccount, BankAccountDetails}

trait ITRegistrationFixtures extends LodgingOfficerFixture {
  val address = ScrsAddress(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"))


  val tradingDetails = TradingDetails(
    tradingNameView = Some(TradingNameView(yesNo = false, tradingName = None)),
    euGoods = Some(false)
  )

  val financials = VatFinancials(
    zeroRatedTurnoverEstimate = None
  )

  val sicAndCompliance = SicAndCompliance(
    description = Some(BusinessActivityDescription("test company desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(SicCode("AB123", "super business", "super business by super people")))
  )

  val vatContact = VatContact(
    digitalContact  = VatDigitalContact("test@test.com", Some("1234567891")),
    ppob            = address
  )

  val voluntaryThreshold = Threshold(
    mandatoryRegistration = false,
    voluntaryReason       = Some(Threshold.SELLS)
  )

  val threshold = Threshold(
    mandatoryRegistration     = true,
    voluntaryReason           = None,
    overThresholdDate         = Some(LocalDate.of(2016, 9, 30)),
    expectedOverThresholdDate = Some(LocalDate.of(2016, 9, 30))
  )

  val flatRateScheme  = FlatRateScheme(joinFrs = Some(false))

  val turnOverEstimates = TurnoverEstimates(vatTaxable = 30000)
  val bankAccount     = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))


  val returns         = Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None)

  val vatReg = VatScheme(
    id                  = "1",
    status              = VatRegStatus.draft,
    tradingDetails      = Some(tradingDetails),
    lodgingOfficer      = None,
    financials          = Some(financials),
    sicAndCompliance    = Some(sicAndCompliance),
    vatContact          = Some(vatContact),
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
    financials          = Some(financials),
    sicAndCompliance    = Some(sicAndCompliance),
    vatContact          = Some(vatContact),
    threshold           = Some(threshold),
    flatRateScheme      = Some(flatRateScheme),
    bankAccount         = Some(bankAccount)
  )

}
