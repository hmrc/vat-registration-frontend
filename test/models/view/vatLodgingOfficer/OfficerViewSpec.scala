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

package models.view.vatLodgingOfficer

import fixtures.VatRegistrationFixture
import models.api._
import models.external.Officer
import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class OfficerViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into an OfficerView" in {
      val testName = Name(Some("forename"), None, "surname")
      val testRole = "role"
      val testDOB = DateOfBirth(1,2,1984)

      val testOfficerView = OfficerView(Officer(testName, testRole, Some(testDOB)))

      val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
      val currentOrPreviousAddress = CurrentOrPreviousAddress(false, Some(address))

      val vatLodgingOfficer = VatLodgingOfficer(
        currentAddress = address,
        dob = testDOB,
        nino = "",
        role = testRole,
        name = testName,
        formerName = FormerName(false, None),
        currentOrPreviousAddress = currentOrPreviousAddress,
        contact = OfficerContactDetails.empty)

      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[OfficerView].toViewModel(vs) shouldBe Some(testOfficerView)
    }

    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[OfficerView].toViewModel(vs) shouldBe None
    }

  }

}
