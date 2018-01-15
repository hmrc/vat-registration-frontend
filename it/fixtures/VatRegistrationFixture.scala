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
import features.returns.{Frequency, Returns, Stagger}
import models.api._
import models.external.Officer
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerSecurityQuestionsView}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.{BankAccount, BankAccountDetails, S4LVatLodgingOfficer}

trait VatRegistrationFixture {
  val address = ScrsAddress(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"))

  val tradingDetails = VatTradingDetails(
    tradingName = TradingName(selection = false, tradingName = None),
    euTrading = VatEuTrading(false, Some(false))
  )

  val lodgingOfficer = VatLodgingOfficer(
    currentAddress = Some(address),
    dob = Some(DateOfBirth(31, 12, 1980)),
    nino = Some("SR123456C"),
    role = Some("Director"),
    name = Some(Name(forename = Some("Firstname"), surname = "lastname", otherForenames = None)),
    changeOfName = Some(ChangeOfName(nameHasChanged = false)),
    currentOrPreviousAddress = Some(CurrentOrPreviousAddress(true)),
    contact = Some(OfficerContactDetails(Some("test@test.com"), None, None))
  )

  val validLodgingOfficerPreIV = VatLodgingOfficer(
    currentAddress = None,
    dob = Some(DateOfBirth(31, 12, 1980)),
    nino = Some("SR123456C"),
    role = Some("Director"),
    name = Some(Name(forename = Some("Firstname"), surname = "lastname", otherForenames = None)),
    changeOfName = None,
    currentOrPreviousAddress = None,
    contact = None
  )

  val validPreIVScheme = VatScheme(id="1", lodgingOfficer = Some(validLodgingOfficerPreIV), status = VatRegStatus.draft)
  val completionCapacity = CompletionCapacity(Name(Some("Bob"), Some("Bimbly Bobblous"), "Bobbings", None), "director")

  val officerName = Name(forename = Some("Firstname"), surname = "lastname", otherForenames = None)

  val validS4LLodgingOfficerPreIv = S4LVatLodgingOfficer (
    completionCapacity = Some(CompletionCapacityView("",Some(completionCapacity))),
    officerSecurityQuestions = Some(OfficerSecurityQuestionsView(LocalDate.of(2017,11,5),"nino",Some(officerName)))
  )

  val validOfficer = Officer(
    name = officerName,
    role = "Director",
    dateOfBirth = Some(DateOfBirth(31, 12, 1980))
  )

  val validCompletionCapacity = CompletionCapacity(
    name = Name(forename = Some("Firstname"), surname = "lastname", otherForenames = None),
    role = "Director"
  )

  val financials = VatFinancials(
    turnoverEstimate = 30000,
    zeroRatedTurnoverEstimate = None
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

  val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")))

  val returns = Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None)

  val vatReg = VatScheme(
    id = "1",
    status = VatRegStatus.draft,
    tradingDetails = Some(tradingDetails),
    lodgingOfficer = Some(lodgingOfficer),
    financials = Some(financials),
    vatSicAndCompliance = Some(sicAndCompliance),
    vatContact = Some(vatContact),
    vatServiceEligibility = Some(eligibility),
    vatFlatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    returns = Some(returns)
  )

  val vatRegIncorporated = VatScheme(
    id = "1",
    status =VatRegStatus.draft,
    tradingDetails = Some(tradingDetails),
    lodgingOfficer = Some(lodgingOfficer),
    financials = Some(financials),
    vatSicAndCompliance = Some(sicAndCompliance),
    vatContact = Some(vatContact),
    vatServiceEligibility = Some(eligibility.copy(vatEligibilityChoice = Some(eligibilityChoiceIncorporated))),
    vatFlatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount)
  )
val validName = Name(Some("foo"),Some("bar"),"fizz",Some("bang"))
  val nameId = validName.id
  val validS4LLodgingOfficer = S4LVatLodgingOfficer(
    completionCapacity = Some(CompletionCapacityView(nameId,Some(CompletionCapacity(validName,"bar")))),
    officerSecurityQuestions = Some(OfficerSecurityQuestionsView(LocalDate.of(2017,11,5),"nino",Some(validName))))

}
