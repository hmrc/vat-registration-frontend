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
import models.ApiModelTransformer
import models.api._
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class CompletionCapacityViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "modelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a CompletionCapacityView" in {
      val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))
      val vatLodgingOfficer = VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName)
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      val expected = CompletionCapacityView(officerName.id, Some(CompletionCapacity(officerName, "director")))

      ApiModelTransformer[CompletionCapacityView].toViewModel(vs) shouldBe Some(expected)
    }


    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[CompletionCapacityView].toViewModel(vs) shouldBe None
    }

  }

}
