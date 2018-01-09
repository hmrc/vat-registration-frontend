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

import features.officer.models.view.ContactDetailsView
import fixtures.VatRegistrationFixture
import models.S4LVatLodgingOfficer
import models.api._
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class ContactDetailsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val officerContactDetailsView = ContactDetailsView(Some("test@test.com"), Some("07837483287"), Some("07827483287"))
  val officerContactDetails = OfficerContactDetails(Some("test@test.com"), Some("07837483287"), Some("07827483287"))

  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a OfficerContactDetailsView" in {
      val emptyName = Name(None, None, "", None)

      val vatLodgingOfficer = VatLodgingOfficer(
        currentAddress = Some(address),
        dob = Some(validDob),
        nino = Some(""),
        role = Some(""),
        name = Some(emptyName),
        changeOfName = Some(changeOfName),
        currentOrPreviousAddress = Some(currentOrPreviousAddress),
        contact = Some(officerContactDetails))
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      S4LVatLodgingOfficer.modelTransformerContactDetails.toViewModel(vs) shouldBe Some(officerContactDetailsView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      S4LVatLodgingOfficer.modelTransformerContactDetails.toViewModel(vs) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerContactDetails = Some(officerContactDetailsView))

    "extract officerContactDetailsView from lodgingOfficer" in {
      S4LVatLodgingOfficer.viewModelFormatContactDetails.read(s4LVatLodgingOfficer) shouldBe Some(officerContactDetailsView)
    }

    "update empty lodgingOfficer with officerContactDetailsView" in {
      S4LVatLodgingOfficer.viewModelFormatContactDetails.update(officerContactDetailsView, Option.empty[S4LVatLodgingOfficer]).
        officerContactDetails shouldBe Some(officerContactDetailsView)
    }

    "update non-empty lodgingOfficer with officerContactDetailsView" in {
      S4LVatLodgingOfficer.viewModelFormatContactDetails.update(officerContactDetailsView, Some(s4LVatLodgingOfficer)).
        officerContactDetails shouldBe Some(officerContactDetailsView)
    }
  }
}
