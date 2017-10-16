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

import models.api._
import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.QUARTERLY
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason

trait VatRegistrationFixture {
  val address = ScrsAddress(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"))

  val tradingDetails = VatTradingDetails(
    vatChoice = VatChoice(vatStartDate = VatStartDate(COMPANY_REGISTRATION_DATE, None)),
    tradingName = TradingName(selection = false, tradingName = None),
    euTrading = VatEuTrading(false, Some(false))
  )

  val lodgingOfficer = VatLodgingOfficer(
    currentAddress = address,
    dob = DateOfBirth(31, 12, 1980),
    nino = "SR123456C",
    role = "Director",
    name = Name(forename = Some("Firstname"), surname = "lastname", otherForenames = None),
    changeOfName = ChangeOfName(nameHasChanged = false),
    currentOrPreviousAddress = CurrentOrPreviousAddress(true),
    contact = OfficerContactDetails(Some("test@test.com"), None, None)
  )

  val financials = VatFinancials(
    bankAccount = None,
    turnoverEstimate = 30000,
    zeroRatedTurnoverEstimate = None,
    reclaimVatOnMostReturns = false,
    accountingPeriods = VatAccountingPeriod(QUARTERLY, Some("jan_apr_jul_oct"))
  )

  val sicAndCompliance = VatSicAndCompliance(
    businessDescription = "test company desc",
    culturalCompliance = None,
    labourCompliance = None,
    financialCompliance = None,
    mainBusinessActivity = SicCode("AB123", "super business", "super business by super people")
  )

  val vatContact = VatContact(
    digitalContact = VatDigitalContact("test@test.com", Some("1234567891")),
    ppob = address
  )

  val eligibilityChoice = VatEligibilityChoice(
    necessity = VatEligibilityChoice.NECESSITY_VOLUNTARY,
    reason = Some(VoluntaryRegistrationReason.SELLS)
  )

  val eligibilityChoiceIncorporated = VatEligibilityChoice(
    necessity = VatEligibilityChoice.NECESSITY_OBLIGATORY,
    reason = None,
    vatThresholdPostIncorp = Some(VatThresholdPostIncorp(true, Some(LocalDate.of(2016, 9, 30)))),
    vatExpectedThresholdPostIncorp = Some(VatExpectedThresholdPostIncorp(true, Some(LocalDate.of(2016, 9, 30))))
  )

  val eligibility = VatServiceEligibility(
    haveNino = Some(true),
    doingBusinessAbroad = Some(false),
    doAnyApplyToYou = Some(false),
    applyingForAnyOf = Some(false),
    applyingForVatExemption = Some(false),
    companyWillDoAnyOf = Some(false),
    vatEligibilityChoice = Some(eligibilityChoice)
  )

  val flatRateScheme = VatFlatRateScheme()

  val vatReg = VatScheme(
    id = "1",
    tradingDetails = Some(tradingDetails),
    lodgingOfficer = Some(lodgingOfficer),
    financials = Some(financials),
    vatSicAndCompliance = Some(sicAndCompliance),
    vatContact = Some(vatContact),
    vatServiceEligibility = Some(eligibility),
    vatFlatRateScheme = Some(flatRateScheme)
  )

  val vatRegIncorporated = VatScheme(
    id = "1",
    tradingDetails = Some(tradingDetails),
    lodgingOfficer = Some(lodgingOfficer),
    financials = Some(financials),
    vatSicAndCompliance = Some(sicAndCompliance),
    vatContact = Some(vatContact),
    vatServiceEligibility = Some(eligibility.copy(vatEligibilityChoice = Some(eligibilityChoiceIncorporated))),
    vatFlatRateScheme = Some(flatRateScheme)
  )
}
