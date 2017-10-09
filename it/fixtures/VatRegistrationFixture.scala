package fixtures

import models.api._
import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.QUARTERLY
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason

trait VatRegistrationFixture {
  val address = ScrsAddress(line1 = "3 Test Building", line2 = "5 Test Road", postcode = Some("TE1 1ST"))

  val tradingDetails = VatTradingDetails(
    vatChoice = VatChoice(vatStartDate = VatStartDate(COMPANY_REGISTRATION_DATE, None)),
    tradingName = TradingName(selection = false, tradingName = None),
    euTrading = VatEuTrading(selection = false)
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
    accountingPeriods = VatAccountingPeriod(QUARTERLY)
  )

  val sicAndCompliance = VatSicAndCompliance(
    businessDescription = "test company desc",
    culturalCompliance = Some(VatComplianceCultural(false)),
    labourCompliance = None,
    financialCompliance = None,
    mainBusinessActivity = SicCode("AB123", "super business", "super business by super people")
  )

  val vatContact = VatContact(
    digitalContact = VatDigitalContact("test@test.com"),
    ppob = address
  )

  val eligibilityChoice = VatEligibilityChoice(
    necessity = VatEligibilityChoice.NECESSITY_VOLUNTARY,
    reason = Some(VoluntaryRegistrationReason.SELLS)
  )

  val eligibility = VatServiceEligibility(
    haveNino = Some(true),
    doingBusinessAbroad = Some(false),
    doAnyApplyToYou = Some(false),
    applyingForAnyOf = Some(false),
    companyWillDoAnyOf = Some(false),
  )

  val vatReg = VatScheme(
    id = "1",
    tradingDetails = Some(tradingDetails),
    lodgingOfficer = Some(lodgingOfficer),
    financials = Some(financials),
    vatSicAndCompliance = Some(sicAndCompliance),
    vatContact = Some(vatContact),
    vatServiceEligibility = Some(),
    vatFlatRateScheme = Some()
  )
}
