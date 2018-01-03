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
import models.{ApiModelTransformer, S4LFlatRateScheme}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class JoinFrsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val testFlatRateScheme = VatFlatRateScheme(joinFrs = true, Some(AnnualCostsInclusiveView.YES))

  "apiModelTransformer" should {

    "convert VatScheme with FlatRateScheme details into a JoinFrsView" in {
      val vs = vatScheme().copy(vatFlatRateScheme = Some(testFlatRateScheme))

      ApiModelTransformer[JoinFrsView].toViewModel(vs) shouldBe Some(JoinFrsView(true))
    }

    "convert VatScheme without FlatRateScheme to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[JoinFrsView].toViewModel(vs) shouldBe None
    }

  }

//  "ViewModelFormat" should {
//    val s4LFlatRateScheme: S4LFlatRateScheme = S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)))
//
//    "extract JoinFrsView from lodgingOfficer" in {
//      JoinFrsView.viewModelFormat.read(s4LFlatRateScheme) shouldBe Some(JoinFrsView(true))
//    }
//
//    "update empty lodgingOfficer with JoinFrsView" in {
//      JoinFrsView.viewModelFormat.update(JoinFrsView(true), Option.empty[S4LFlatRateScheme])
//        .joinFrs shouldBe Some(JoinFrsView(true))
//    }
//
//    "update non-empty lodgingOfficer with JoinFrsView" in {
//      JoinFrsView.viewModelFormat.update(JoinFrsView(false), Some(s4LFlatRateScheme))
//        .joinFrs shouldBe Some(JoinFrsView(false))
//    }
//  }

}
