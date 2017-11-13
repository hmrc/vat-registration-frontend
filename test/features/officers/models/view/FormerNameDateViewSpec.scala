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
import models.external.Officer
import models.{ApiModelTransformer, DateModel, S4LVatLodgingOfficer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class FormerNameDateViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  override val changeOfName = ChangeOfName(true, Some(FormerName("Bob", Some(validStartDate))))
  val testFormerNameDateView = FormerNameDateView(validStartDate)
  val differentFormerNameDateView = FormerNameDateView(validDob)

  val anOfficer = Officer(Name(Some("name1"), Some("name2"),"SurName"), "director", Some(validDob))
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  val date = LocalDate.of(2017, 3, 21)

  "unbind" should {
    "decompose valid StartDate" in {
      val testStartDate = FormerNameDateView(validStartDate)
      inside(FormerNameDateView.unbind(testStartDate)) {
        case Some(dateModel) =>
          dateModel shouldBe DateModel.fromLocalDate(validStartDate)
      }
    }
  }

  "bind" should {
    "create StartDate when DateModel is present" in {
      FormerNameDateView.bind(DateModel.fromLocalDate(validStartDate)) shouldBe FormerNameDateView(validStartDate)
    }

  }

  "apiModelTransformer" should {
    "convert VatScheme with VatLodgingOfficer details into a FormerNameDateView" in {
      val vatLodgingOfficer = VatLodgingOfficer(
        currentAddress = Some(address),
        dob = Some(validDob),
        nino = Some(""),
        role = Some("director"),
        name = Some(officerName),
        changeOfName = Some(changeOfName),
        currentOrPreviousAddress = Some(currentOrPreviousAddress),
        contact = Some(validOfficerContactDetails))

      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      ApiModelTransformer[FormerNameDateView].toViewModel(vs) shouldBe Some(testFormerNameDateView)
    }

    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[FormerNameDateView].toViewModel(vs) shouldBe None
    }

  }

  "apply" should {
    "create a FormerNameDateView instance" in {
      FormerNameDateView(validStartDate) shouldBe testFormerNameDateView
    }
  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(formerNameDate = Some(testFormerNameDateView))

    "extract formerNameView from lodgingOfficer" in {
      FormerNameDateView.viewModelFormat.read(s4LVatLodgingOfficer) shouldBe Some(testFormerNameDateView)
    }

    "update empty lodgingOfficer with formerNameDateView" in {
      FormerNameDateView.viewModelFormat.update(testFormerNameDateView, Option.empty[S4LVatLodgingOfficer]).formerNameDate shouldBe Some(testFormerNameDateView)
    }

    "update non-empty lodgingOfficer with formerNameDateView" in {
      FormerNameDateView.viewModelFormat.update(testFormerNameDateView, Some(s4LVatLodgingOfficer)).formerNameDate shouldBe Some(testFormerNameDateView)
    }

  }
}
