/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.fixtures

import java.time.LocalDate

import features.officer.models.view._
import models.api.ScrsAddress
import models.external.{Name, Officer}

trait LodgingOfficerFixtures {

  val validOfficer = Officer(
    name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
    role = "Director"
  )

  val emptyLodgingOfficer = LodgingOfficer(None, None, None, None, None, None)

  val validCurrentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

  val validPrevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

  val validPartialLodgingOfficer = LodgingOfficer(
    securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12))),
    homeAddress = None,
    contactDetails = None,
    formerName = None,
    formerNameDate = None,
    previousAddress = None
  )

  val validFullLodgingOfficer = LodgingOfficer(
    securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12))),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, Some(validPrevAddress)))
  )
}
