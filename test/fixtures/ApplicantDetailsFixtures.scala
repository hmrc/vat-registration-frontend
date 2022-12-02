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

import models._
import models.api.Address
import models.external._
import models.external.soletraderid.OverseasIdentifierDetails

import java.time.LocalDate

trait ApplicantDetailsFixtures {

  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testApplicantNino = "AB123456C"
  val testApplicantDob = LocalDate.of(2020, 1, 1)
  val testRole = Some(Director)
  val validCurrentAddress = Address(line1 = "Test Line1", line2 = Some("Test Line2"), postcode = Some("TE 1ST"), addressValidated = true)
  val validPrevAddress = Address(line1 = "Test Line11", line2 = Some("Test Line22"), postcode = Some("TE1 1ST"), addressValidated = true)

  val testOverseasIdentifier = "1234567890"
  val testOverseasIdentifierCountry = "EE"
  val testOverseasIdentifierDetails = OverseasIdentifierDetails(testOverseasIdentifier, testOverseasIdentifierCountry)
  val testOverseasCountryName = "Estonia"
  val testCrn = "testCrn"
  val testChrn = "testChrn"
  val testCasc = "testCasc"
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate = LocalDate.of(2020, 2, 3)

  val emptyApplicantDetails = ApplicantDetails()

  val testBpSafeId = "testBpId"

  val testPersonalDetails = PersonalDetails(testFirstName, testLastName, Some(testApplicantNino), None, identifiersMatch = true, Some(testApplicantDob))

  val testLimitedCompany: IncorporatedEntity = IncorporatedEntity(
    testCrn,
    Some(testCompanyName),
    Some(testCtUtr),
    None,
    Some(testIncorpDate),
    "GB",
    identifiersMatch = true,
    RegisteredStatus,
    Some(BvPass),
    Some(testBpSafeId)
  )

  val completeApplicantDetails = ApplicantDetails(
    entity = Some(testLimitedCompany),
    personalDetails = Some(testPersonalDetails),
    currentAddress = Some(validCurrentAddress),
    contact = DigitalContactOptional(
      email = Some("test@t.test"),
      emailVerified = Some(true),
      tel = Some("1234")
    ),
    changeOfName = FormerName(
      hasFormerName = Some(true),
      name = Some(Name(Some("New"), Some("Name"), "Cosmo")),
      change = Some(LocalDate.of(2000, 7, 12))
    ),
    noPreviousAddress = Some(false),
    previousAddress = Some(validPrevAddress),
    roleInTheBusiness = testRole
  )

  val testSautr = "1234567890"
  val testTrn = "testTrn"
  val testIncorpCountry = "GB"
  val testPostcode = "AA11AA"
  val testRegistration: BusinessRegistrationStatus = RegisteredStatus
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
    identifiersMatch = true
  )

  val soleTraderApplicantDetails: ApplicantDetails = ApplicantDetails(
    entity = Some(testSoleTrader),
    personalDetails = Some(testPersonalDetails),
    currentAddress = Some(validCurrentAddress),
    contact = DigitalContactOptional(
      email = Some("test@t.test"),
      emailVerified = Some(true),
      tel = Some("1234")
    ),
    changeOfName = FormerName(
      hasFormerName = Some(false)
    ),
    noPreviousAddress = Some(false),
    previousAddress = Some(validPrevAddress)
  )

  val testGeneralPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testLimitedPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = None,
    companyNumber = Some(testCrn),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testScottishPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testScottishLimitedPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = None,
    companyNumber = Some(testCrn),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testLimitedLiabilityPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = None,
    companyNumber = Some(testCrn),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testTrust: MinorEntity = MinorEntity(
    sautr = Some(testSautr),
    ctutr = None,
    postCode = Some(testPostcode),
    chrn = Some(testChrn),
    casc = None,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testUnincorpAssoc: MinorEntity = MinorEntity(
    sautr = Some(testSautr),
    ctutr = None,
    postCode = Some(testPostcode),
    chrn = Some(testChrn),
    casc = Some(testCasc),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testNonUkCompany: MinorEntity = MinorEntity(
    sautr = Some(testSautr),
    ctutr = None,
    overseas = Some(testOverseasIdentifierDetails),
    postCode = None,
    chrn = Some(testChrn),
    casc = None,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testRegisteredSociety: IncorporatedEntity = IncorporatedEntity(
    companyNumber = testCrn,
    companyName = Some(testCompanyName),
    ctutr = Some(testCtUtr),
    chrn = None,
    dateOfIncorporation = Some(testIncorpDate),
    countryOfIncorporation = testIncorpCountry,
    identifiersMatch = true,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId)
  )

  val testCharitableOrganisation: IncorporatedEntity = IncorporatedEntity(
    companyNumber = testCrn,
    companyName = Some(testCompanyName),
    ctutr = None,
    chrn = Some(testChrn),
    dateOfIncorporation = Some(testIncorpDate),
    countryOfIncorporation = testIncorpCountry,
    identifiersMatch = true,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId)
  )
}
