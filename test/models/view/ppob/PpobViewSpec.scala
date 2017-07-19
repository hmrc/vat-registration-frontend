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

package models.view.ppob

import fixtures.VatRegistrationFixture
import models.api.{ScrsAddress, VatScheme}
import models.{ApiModelTransformer, S4LPpob, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec


class PpobViewSpec extends UnitSpec with VatRegistrationFixture with Inside {


  "ApiModelTransformer" should {

    "convert VatScheme without Ppob to empty view model" in {
      val vs = vatScheme().copy(ppob = None)
      ApiModelTransformer[PpobView].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with Ppob section to view model" in {
      val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
      val ppobAddress =address
      val vs = vatScheme().copy(ppob = Some(ppobAddress))

      val expectedPpobView = PpobView(address.id, Some(address))

      ApiModelTransformer[PpobView].toViewModel(vs) shouldBe Some(expectedPpobView)
    }
  }


  "ViewModelFormat" should {
    val testAddress = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
    val testAddressView = PpobView(testAddress.id, Some(testAddress))
    val s4LPpob: S4LPpob = S4LPpob(address = Some(testAddressView))

    "extract address from PpobView" in {
      PpobView.viewModelFormat.read(s4LPpob) shouldBe Some(testAddressView)
    }

    "update empty lodgingOfficer with PpobView" in {
      PpobView.viewModelFormat.update(testAddressView, Option.empty[S4LPpob]).
        address shouldBe Some(testAddressView)
    }

    "update non-empty lodgingOfficer with PpobView" in {
      PpobView.viewModelFormat.update(testAddressView, Some(s4LPpob)).
        address shouldBe Some(testAddressView)
    }
  }
}
