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

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api._
import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class OfficerDateOfBirthViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val testDOB = DateOfBirth(1,12,1999)
  val testDOBView = OfficerDateOfBirthView(LocalDate.of(1999,12,1), Some(Name.empty))

  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a OfficerDateOfBirthView" in {
      val vatLodgingOfficer = VatLodgingOfficer(address, testDOB, "", "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[OfficerDateOfBirthView].toViewModel(vs) shouldBe Some(testDOBView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[OfficerDateOfBirthView].toViewModel(vs) shouldBe None
    }

  }

  "viewModelTransformer" should {
    "update logical group given a component" in {
      val initialVatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)
      val updatedVatLodgingOfficer = VatLodgingOfficer(address, testDOB, "", "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)

      ViewModelTransformer[OfficerDateOfBirthView, VatLodgingOfficer].
        toApi(testDOBView, initialVatLodgingOfficer) shouldBe updatedVatLodgingOfficer
    }
  }

  "VMReads" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerDateOfBirth = Some(testDOBView))

    "extract OfficerDateOfBirthView from lodgingOfficer" in {
      OfficerDateOfBirthView.vmReads.read(s4LVatLodgingOfficer) shouldBe Some(testDOBView)
    }

    "update empty lodgingOfficer with OfficerDateOfBirthView" in {
      OfficerDateOfBirthView.vmReads.update(testDOBView, Option.empty[S4LVatLodgingOfficer]).
        officerDateOfBirth shouldBe Some(testDOBView)
    }

    "update non-empty lodgingOfficer with OfficerDateOfBirthView" in {
      OfficerDateOfBirthView.vmReads.update(testDOBView, Some(s4LVatLodgingOfficer)).
        officerDateOfBirth shouldBe Some(testDOBView)
    }
  }
}
