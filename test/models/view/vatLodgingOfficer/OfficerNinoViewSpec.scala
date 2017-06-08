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

class OfficerNinoViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val testNino = "NB666666D"
  val testNinoView = OfficerNinoView(testNino)


  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a OfficerDateOfBirthView" in {
      val vatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, testNino, "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[OfficerNinoView].toViewModel(vs) shouldBe Some(testNinoView)
    }

    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[OfficerNinoView].toViewModel(vs) shouldBe None
    }

  }

  "viewModelTransformer" should {
    "update logical group given a component" in {
      val initialVatLodgingOfficer =
        VatLodgingOfficer(address, DateOfBirth.empty, "", "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)
      val updatedVatLodgingOfficer =
        VatLodgingOfficer(address, DateOfBirth.empty, testNino, "", Name.empty, FormerName(false, None), currentOrPreviousAddress, OfficerContactDetails.empty)

      ViewModelTransformer[OfficerNinoView, VatLodgingOfficer].
        toApi(testNinoView, initialVatLodgingOfficer) shouldBe updatedVatLodgingOfficer
    }
  }


  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerNino = Some(testNinoView))

    "extract OfficerNinoView from lodgingOfficer" in {
      OfficerNinoView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(testNinoView)
    }

    "update empty lodgingOfficer with OfficerNinoView" in {
      OfficerNinoView.viewModelFormat.update(testNinoView, Option.empty[S4LVatLodgingOfficer]).
        officerNino shouldBe Some(testNinoView)
    }

    "update non-empty lodgingOfficer with OfficerNinoView" in {
      OfficerNinoView.viewModelFormat.update(testNinoView, Some(s4LVatLodgingOfficer)).
        officerNino shouldBe Some(testNinoView)
    }
  }
}
