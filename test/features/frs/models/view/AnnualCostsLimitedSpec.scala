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
import models.api.VatFlatRateScheme
import models.{ApiModelTransformer, S4LFlatRateScheme}
import org.scalatest.{Inspectors, Matchers}
import uk.gov.hmrc.play.test.UnitSpec

class AnnualCostsLimitedSpec extends UnitSpec with Matchers with Inspectors with VatRegistrationFixture {

  private val validationFunction = AnnualCostsLimitedView.valid

  "ApiModelTransformer" should {

    "produce empty view model from an empty annual costs limited" in {
      val vm = ApiModelTransformer[AnnualCostsLimitedView]
        .toViewModel(vatScheme(vatFlatRateScheme = None))
      vm shouldBe None
    }

    "produce a view model from a vatScheme with annual costs limited (answer YES)" in {
      val vm = ApiModelTransformer[AnnualCostsLimitedView]
        .toViewModel(vatScheme(vatFlatRateScheme = Some(VatFlatRateScheme(annualCostsLimited = Some(AnnualCostsLimitedView.YES)))))
      vm shouldBe Some(AnnualCostsLimitedView(AnnualCostsLimitedView.YES))
    }

    "produce a view model from a vatScheme with annual costs limited (answer NO)" in {
      val vm = ApiModelTransformer[AnnualCostsLimitedView]
        .toViewModel(vatScheme(vatFlatRateScheme = Some(VatFlatRateScheme(annualCostsLimited = Some(AnnualCostsLimitedView.NO)))))
      vm shouldBe Some(AnnualCostsLimitedView(AnnualCostsLimitedView.NO))
    }

    "produce a view model from a vatScheme with annual costs limited (answer YES within 12 months)" in {
      val vm = ApiModelTransformer[AnnualCostsLimitedView]
        .toViewModel(vatScheme(vatFlatRateScheme = Some(VatFlatRateScheme(annualCostsLimited = Some(AnnualCostsLimitedView.YES_WITHIN_12_MONTHS)))))
      vm shouldBe Some(AnnualCostsLimitedView(AnnualCostsLimitedView.YES_WITHIN_12_MONTHS))
    }

  }

  "AnnualCostsLimitedView is valid" when {

    "selected answer is one of the allowed values" in {
      forAll(Seq(AnnualCostsLimitedView.YES, AnnualCostsLimitedView.YES_WITHIN_12_MONTHS, AnnualCostsLimitedView.NO)) {
        validationFunction(_) shouldBe true
      }
    }

  }

  "AnnualCostsLimitedView is NOT valid" when {

    "selected reason is not of the allowed values" in {
      forAll(Seq("", "not an allowed value")) {
        validationFunction(_) shouldBe false
      }
    }

  }
}
