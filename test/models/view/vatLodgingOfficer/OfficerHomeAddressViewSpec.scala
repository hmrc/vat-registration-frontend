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
import models.api.{DateOfBirth, ScrsAddress, VatLodgingOfficer}
import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec


class OfficerHomeAddressViewSpec extends UnitSpec with VatRegistrationFixture with Inside {


  "ApiModelTransformer" should {

    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[OfficerHomeAddressView].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with VatLodgingOfficer section to view model" in {
      val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
      val vatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      val expectedOfficerHomeAddressView = OfficerHomeAddressView(address.id, Some(address))

      ApiModelTransformer[OfficerHomeAddressView].toViewModel(vs) shouldBe Some(expectedOfficerHomeAddressView)
    }
  }


  "ViewModelFormat" should {
    val testAddress = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
    val testAddressView = OfficerHomeAddressView(testAddress.id, Some(testAddress))
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerHomeAddress = Some(testAddressView))

    "extract OfficerHomeAddressView from lodgingOfficer" in {
      OfficerHomeAddressView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(testAddressView)
    }

    "update empty lodgingOfficer with OfficerHomeAddressView" in {
      OfficerHomeAddressView.viewModelFormat.update(testAddressView, Option.empty[S4LVatLodgingOfficer]).
        officerHomeAddress shouldBe Some(testAddressView)
    }

    "update non-empty lodgingOfficer with OfficerHomeAddressView" in {
      OfficerHomeAddressView.viewModelFormat.update(testAddressView, Some(s4LVatLodgingOfficer)).
        officerHomeAddress shouldBe Some(testAddressView)
    }
  }
}
