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
import models.{ApiModelTransformer, S4LVatLodgingOfficer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class OfficerContactDetailsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val officerContactDetailsView = OfficerContactDetailsView(Some("test@test.com"), Some("07837483287"), Some("07827483287"))
  val officerContactDetails = OfficerContactDetails(Some("test@test.com"), Some("07837483287"), Some("07827483287"))

  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a OfficerContactDetailsView" in {
      val emptyName = Name(None, None, "", None)

      val vatLodgingOfficer = VatLodgingOfficer(
        currentAddress = address,
        dob = validDob,
        nino = "",
        role = "",
        name = emptyName,
        changeOfName = changeOfName,
        currentOrPreviousAddress = currentOrPreviousAddress,
        contact = officerContactDetails)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[OfficerContactDetailsView].toViewModel(vs) shouldBe Some(officerContactDetailsView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[OfficerContactDetailsView].toViewModel(vs) shouldBe None
    }

  }

  "apply" should {
    "create a OfficerContactDetailsView instance" in {
      OfficerContactDetailsView(officerContactDetails) shouldBe officerContactDetailsView
    }
  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerContactDetails = Some(officerContactDetailsView))

    "extract officerContactDetailsView from lodgingOfficer" in {
      OfficerContactDetailsView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(officerContactDetailsView)
    }

    "update empty lodgingOfficer with officerContactDetailsView" in {
      OfficerContactDetailsView.viewModelFormat.update(officerContactDetailsView, Option.empty[S4LVatLodgingOfficer]).
        officerContactDetails shouldBe Some(officerContactDetailsView)
    }

    "update non-empty lodgingOfficer with officerContactDetailsView" in {
      OfficerContactDetailsView.viewModelFormat.update(officerContactDetailsView, Some(s4LVatLodgingOfficer)).
        officerContactDetails shouldBe Some(officerContactDetailsView)
    }
  }
}