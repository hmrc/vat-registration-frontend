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
import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class CompletionCapacityViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val anOfficer = CompletionCapacity(Name(Some("name1"), Some("name2"),"SurName"), "director")
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a CompletionCapacityView" in {
      val vatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      val expected = CompletionCapacityView(officerName.id, Some(CompletionCapacity(officerName, "director")))

      ApiModelTransformer[CompletionCapacityView].toViewModel(vs) shouldBe Some(expected)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[CompletionCapacityView].toViewModel(vs) shouldBe None
    }

  }

  "viewModelTransformer" should {
    "update logical group given a component" in {
      val ccv = CompletionCapacityView(anOfficer)
      val initialVatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "", Name.empty, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)
      val updatedVatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "director", anOfficer.name, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)

      ViewModelTransformer[CompletionCapacityView, VatLodgingOfficer].
        toApi(ccv, initialVatLodgingOfficer) shouldBe updatedVatLodgingOfficer
    }
  }


  "apply" should {
    "create a CompletionCapacityView instance with the correct id" in {
      val ccv = CompletionCapacityView(anOfficer)

      ccv.id shouldBe anOfficer.name.id
    }
  }

  "ViewModelFormat" should {
    val ccv = CompletionCapacityView(anOfficer)
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(completionCapacity = Some(ccv))

    "extract completionCapacityView from lodgingOfficer" in {
      CompletionCapacityView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(ccv)
    }

    "update empty lodgingOfficer with completionCapacityView" in {
      CompletionCapacityView.viewModelFormat.update(ccv, Option.empty[S4LVatLodgingOfficer]).completionCapacity shouldBe Some(ccv)
    }

    "update non-empty lodgingOfficer with completionCapacityView" in {
      CompletionCapacityView.viewModelFormat.update(ccv, Some(s4LVatLodgingOfficer)).completionCapacity shouldBe Some(ccv)
    }
  }
}
