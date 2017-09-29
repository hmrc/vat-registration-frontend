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
import models.{ApiModelTransformer, S4LVatLodgingOfficer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class FormerNameViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  override val changeOfName = ChangeOfName(true, Some(FormerName("Bob")))
  val testFormerNameView = FormerNameView(true, Some("Bob"))

  val anOfficer = Officer(Name(Some("name1"), Some("name2"),"SurName"), "director", Some(validDob))
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a FormerNameView" in {
      val vatLodgingOfficer =
        VatLodgingOfficer(address, validDob, "", "director", officerName,  changeOfName, currentOrPreviousAddress, validOfficerContactDetails)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[FormerNameView].toViewModel(vs) shouldBe Some(testFormerNameView)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[FormerNameView].toViewModel(vs) shouldBe None
    }

  }

  "apply" should {
    "create a FormerNameView instance" in {
      FormerNameView(changeOfName) shouldBe testFormerNameView
    }
  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(formerName = Some(testFormerNameView))

    "extract formerNameView from lodgingOfficer" in {
      FormerNameView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(testFormerNameView)
    }

    "update empty lodgingOfficer with formerNameView" in {
      FormerNameView.viewModelFormat.update(testFormerNameView, Option.empty[S4LVatLodgingOfficer]).formerName shouldBe Some(testFormerNameView)
    }

    "update non-empty lodgingOfficer with formerNameView" in {
      FormerNameView.viewModelFormat.update(testFormerNameView, Some(s4LVatLodgingOfficer)).formerName shouldBe Some(testFormerNameView)
    }

  }
}