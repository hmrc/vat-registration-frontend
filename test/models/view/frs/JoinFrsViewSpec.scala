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

package models.view.frs

import fixtures.VatRegistrationFixture
import models.ApiModelTransformer
import models.api._
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class JoinFrsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val testFlatRateScheme = FlatRateScheme(joinFrs = true, AnnualCostsInclusiveView.YES, registerForFrs = true)

  "apiModelTransformer" should {

    "convert VatScheme with FlatRateScheme details into a JoinFrsView" in {
      val vs = vatScheme().copy(flatRateScheme = Some(testFlatRateScheme))

      ApiModelTransformer[JoinFrsView].toViewModel(vs) shouldBe Some(JoinFrsView(true))
    }

    "convert VatScheme without FlatRateScheme to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      ApiModelTransformer[JoinFrsView].toViewModel(vs) shouldBe None
    }

  }

  //  "viewModelTransformer" should {
  //    "update logical group given a component" in {
  //      val initialFlatRateScheme: JoinFrsView = JoinFrsView(false)
  //      val updatedFlatRateScheme: JoinFrsView = JoinFrsView(true)
  //
  //      ViewModelTransformer[JoinFrsView, FlatRateScheme].
  //        toApi(testFlatRateScheme, initialFlatRateScheme) shouldBe updatedFlatRateScheme
  //    }
  //  }
  //
  //
  //  "ViewModelFormat" should {
  //    val s4LFlatRateScheme: S4LFlatRateScheme = S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)))
  //
  //    "extract JoinFrsView from lodgingOfficer" in {
  //      JoinFrsView.viewModelFormat.read(s4LFlatRateScheme) shouldBe Some(testNinoView)
  //    }
  //
  //    "update empty lodgingOfficer with JoinFrsView" in {
  //      JoinFrsView.viewModelFormat.update(testNinoView, Option.empty[S4LFlatRateScheme]).
  //        officerNino shouldBe Some(testNinoView)
  //    }
  //
  //    "update non-empty lodgingOfficer with JoinFrsView" in {
  //      JoinFrsView.viewModelFormat.update(testNinoView, Some(s4LFlatRateScheme)).
  //        officerNino shouldBe Some(testNinoView)
  //    }
  //  }

}
