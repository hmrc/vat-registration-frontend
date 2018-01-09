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

package models.view.vatLodgingOfficer

import features.officer.models.view.PreviousAddressView
import fixtures.VatRegistrationFixture
import models.api._
import models.external.Officer
import models.S4LVatLodgingOfficer
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class PreviousAddressViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val anOfficer = Officer(Name(Some("name1"), Some("name2"),"SurName"), "director", Some(validDob))
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  val testPreviousAddress = CurrentOrPreviousAddress(currentAddressThreeYears = true, previousAddress = Some(address))
  val testPreviousAddressView = PreviousAddressView(true, Some(address))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a PreviousAddressView" in {
      val vatLodgingOfficer =
        VatLodgingOfficer(
          currentAddress = Some(address),
          dob = Some(validDob),
          nino = Some(""),
          role = Some("director"),
          name = Some(officerName),
          changeOfName = Some(changeOfName),
          currentOrPreviousAddress = Some(testPreviousAddress),
          contact = Some(validOfficerContactDetails))

      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      S4LVatLodgingOfficer.modelTransformerPreviousAddress.toViewModel(vs) shouldBe Some(testPreviousAddressView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      S4LVatLodgingOfficer.modelTransformerPreviousAddress.toViewModel(vs) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(previousAddress = Some(testPreviousAddressView))

    "extract previousAddressView from lodgingOfficer" in {
      S4LVatLodgingOfficer.viewModelFormatPreviousAddress.read(s4LVatLodgingOfficer) shouldBe Some(testPreviousAddressView)
    }

    "update empty lodgingOfficer with previousAddressView" in {
      S4LVatLodgingOfficer.viewModelFormatPreviousAddress.update(testPreviousAddressView, Option.empty[S4LVatLodgingOfficer]).previousAddress shouldBe Some(testPreviousAddressView)
    }

    "update non-empty lodgingOfficer with previousAddressView" in {
      S4LVatLodgingOfficer.viewModelFormatPreviousAddress.update(testPreviousAddressView, Some(s4LVatLodgingOfficer)).previousAddress shouldBe Some(testPreviousAddressView)
    }

  }
}
