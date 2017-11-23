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
import models.api._
import models.{ApiModelTransformer, S4LFlatRateScheme}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class BusinessSectorViewSpec extends UnitSpec with VatRegistrationFixture with Inspectors {

  val testFlatRateScheme = VatFlatRateScheme(categoryOfBusiness =Some("Code refactoring"), percentage = Some(14.33))

  "business sector view" should {
    "display decimals correctly" in {
      val inputsOutputs = Seq(
        BigDecimal(6) -> "6",
        BigDecimal(6.5) -> "6.5",
        BigDecimal(6.66) -> "6.66",
        BigDecimal(6.666) -> "6.67"
        )
      forAll(inputsOutputs){
        case (in:BigDecimal, out:String) => BusinessSectorView("any", in).flatRatePercentageFormatted shouldBe out
      }
    }
  }

  "apiModelTransformer" should {

    "convert VatScheme with FlatRateScheme details into a BusinessSectorView" in {
      val vs = vatScheme().copy(vatFlatRateScheme = Some(testFlatRateScheme))

      ApiModelTransformer[BusinessSectorView].toViewModel(vs) shouldBe Some(BusinessSectorView("Code refactoring", 14.33))
    }

    "convert VatScheme without FlatRateScheme to empty view model" in {
      val vs = vatScheme().copy(vatFlatRateScheme = None)
      ApiModelTransformer[BusinessSectorView].toViewModel(vs) shouldBe None
    }

  }
}
