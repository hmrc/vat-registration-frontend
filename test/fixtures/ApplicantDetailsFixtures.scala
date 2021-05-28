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

import java.time.LocalDate
import models.api.Address
import models.external.incorporatedentityid.{BvPass, IncorporationDetails}
import models.external.{EmailAddress, EmailVerified}
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber, TransactorDetails}

trait ApplicantDetailsFixtures {

  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testApplicantNino = "AB123456C"
  val testApplicantDob = LocalDate.of(2020, 1, 1)
  val testRole = Some(Director)
  val validCurrentAddress = Address(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"), addressValidated = true)
  val validPrevAddress = Address(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"), addressValidated = true)

  val testCrn = "testCrn"
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

  val testIncorpDetails = IncorporationDetails(testCrn, testCompanyName, testCtUtr, testIncorpDate, "GB", true, Some("REGISTERED"), Some(BvPass), Some(testBpSafeId))

  val completeApplicantDetails = ApplicantDetails(
    entity = Some(testIncorpDetails),
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
}
