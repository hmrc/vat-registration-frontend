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

package models.view.frs

import fixtures.VatRegistrationFixture
import models.api._
import models.{ApiModelTransformer, RegisterForFrsView, S4LFlatRateScheme}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class RegisterForFrsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val testFlatRateScheme = VatFlatRateScheme(doYouWantToUseThisRate = Some(true))

  "apiModelTransformer" should {

    "convert VatScheme with FlatRateScheme details into a RegisterForFrsView" in {
      val vs = vatScheme().copy(vatFlatRateScheme = Some(testFlatRateScheme))

      ApiModelTransformer[RegisterForFrsView].toViewModel(vs) shouldBe Some(RegisterForFrsView(true))
    }

    "convert VatScheme without FlatRateScheme to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[RegisterForFrsView].toViewModel(vs) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val s4LFlatRateScheme: S4LFlatRateScheme = S4LFlatRateScheme(registerForFrs = Some(RegisterForFrsView(true)))

    "extract RegisterForFrsView from lodgingOfficer" in {
      RegisterForFrsView.viewModelFormat.read(s4LFlatRateScheme) shouldBe Some(RegisterForFrsView(true))
    }

    "update empty lodgingOfficer with RegisterForFrsView" in {
      RegisterForFrsView.viewModelFormat.update(RegisterForFrsView(true), Option.empty[S4LFlatRateScheme])
        .registerForFrs shouldBe Some(RegisterForFrsView(true))
    }

    "update non-empty lodgingOfficer with RegisterForFrsView" in {
      RegisterForFrsView.viewModelFormat.update(RegisterForFrsView(false), Some(s4LFlatRateScheme))
        .registerForFrs shouldBe Some(RegisterForFrsView(false))
    }
  }

}
