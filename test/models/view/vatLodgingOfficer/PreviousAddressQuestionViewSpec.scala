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

class PreviousAddressQuestionViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val anOfficer = Officer(Name(Some("name1"), Some("name2"),"SurName"), "director", Some(validDob))
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  val testPreviousAddressQuestion = CurrentOrPreviousAddress(currentAddressThreeYears = true, previousAddress = Some(address))
  val testPreviousAddressQuestionView = PreviousAddressQuestionView(true)

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a PreviousAddressQuestionView" in {
      val vatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName, FormerName(true, None), testPreviousAddressQuestion, validOfficerContactDetails)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[PreviousAddressQuestionView].toViewModel(vs) shouldBe Some(testPreviousAddressQuestionView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[PreviousAddressQuestionView].toViewModel(vs) shouldBe None
    }

  }

  "viewModelTransformer" should {
    "update logical group given a component" in {
      val initialVatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "", Name.empty, FormerName(true, None), CurrentOrPreviousAddress(false, Some(address)), validOfficerContactDetails)
      val updatedVatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "", Name.empty, FormerName(true, None), testPreviousAddressQuestion, validOfficerContactDetails)

      ViewModelTransformer[PreviousAddressQuestionView, VatLodgingOfficer].
        toApi(testPreviousAddressQuestionView, initialVatLodgingOfficer) shouldBe updatedVatLodgingOfficer
    }
  }

  "VMReads" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(previousAddressQuestion = Some(testPreviousAddressQuestionView))

    "extract previousAddressQuestionView from lodgingOfficer" in {
      PreviousAddressQuestionView.vmReads.read(s4LVatLodgingOfficer) shouldBe Some(testPreviousAddressQuestionView)
    }

    "update empty lodgingOfficer with previousAddressQuestionView" in {
      PreviousAddressQuestionView.vmReads.udpate(testPreviousAddressQuestionView, Option.empty[S4LVatLodgingOfficer]).previousAddressQuestion shouldBe Some(testPreviousAddressQuestionView)
    }

    "update non-empty lodgingOfficer with previousAddressQuestionView" in {
      PreviousAddressQuestionView.vmReads.udpate(testPreviousAddressQuestionView, Some(s4LVatLodgingOfficer)).previousAddressQuestion shouldBe Some(testPreviousAddressQuestionView)
    }

  }
}
