/*
 * Copyright 2021 HM Revenue & Customs
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

import models.api.Address
import models.external._
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber, TransactorDetails}

import java.time.LocalDate

trait ApplicantDetailsFixtures {

  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testApplicantNino = "AB123456C"
  val testApplicantDob = LocalDate.of(2020, 1, 1)
  val testRole = Some(Director)
  val validCurrentAddress = Address(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"), addressValidated = true)
  val validPrevAddress = Address(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"), addressValidated = true)

  val testCrn = "testCrn"
  val testChrn = "testChrn"
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate = LocalDate.of(2020, 2, 3)

  val emptyApplicantDetails = ApplicantDetails(
    transactor = None,
    homeAddress = None,
    emailAddress = None,
    emailVerified = None,
    telephoneNumber = None,
    formerName = None,
    formerNameDate = None,
    previousAddress = None,
    roleInTheBusiness = None,
    entity = None
  )

  val testBpSafeId = "testBpId"

  val testTransactorDetails = TransactorDetails(testFirstName, testLastName, testApplicantNino, testApplicantDob)

  val testLimitedCompany: IncorporatedEntity = IncorporatedEntity(
    testCrn,
    testCompanyName,
    Some(testCtUtr),
    None,
    testIncorpDate,
    "GB",
    identifiersMatch = true,
    Some("REGISTERED"),
    Some(BvPass),
    Some(testBpSafeId)
  )

  val completeApplicantDetails = ApplicantDetails(
    entity = Some(testLimitedCompany),
    transactor = Some(testTransactorDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    roleInTheBusiness = testRole,
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(false, Some(validPrevAddress)))
  )

  val testSautr = "1234567890"
  val testIncorpCountry = "GB"
  val testPostcode = "AA11AA"
  val testRegistration = "REGISTERED"
  val testSafeId = "X00000123456789"

  val testSoleTrader: SoleTrader = SoleTrader(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = testApplicantNino,
    sautr = Some(testSautr),
    registration = testRegistration,
    businessVerification = BvPass,
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val soleTraderApplicantDetails: ApplicantDetails = ApplicantDetails(
    entity = Some(testSoleTrader),
    transactor = Some(testTransactorDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    roleInTheBusiness = None,
    formerName = Some(FormerNameView(yesNo = false, None)),
    formerNameDate = None,
    previousAddress = Some(PreviousAddressView(yesNo = false, Some(validPrevAddress)))
  )

  val testGeneralPartnership: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    postCode = Some(testPostcode),
    chrn = None,
    registration = testRegistration,
    businessVerification = BvPass,
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testTrust: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    postCode = None,
    chrn = Some(testChrn),
    registration = testRegistration,
    businessVerification = BvPass,
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
  )

  val testRegisteredSociety: IncorporatedEntity = IncorporatedEntity(
    companyNumber = testCrn,
    companyName = testCompanyName,
    ctutr = Some(testCtUtr),
    chrn = None,
    dateOfIncorporation = testIncorpDate,
    countryOfIncorporation = testIncorpCountry,
    identifiersMatch = true
  )

  val testCharitableOrganisation: IncorporatedEntity = IncorporatedEntity(
    companyNumber = testCrn,
    companyName = testCompanyName,
    ctutr = None,
    chrn = Some(testChrn),
    dateOfIncorporation = testIncorpDate,
    countryOfIncorporation = testIncorpCountry,
    identifiersMatch = true
  )
}
