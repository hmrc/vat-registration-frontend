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
import models.api.VatFlatRateScheme
import models.{ApiModelTransformer, S4LFlatRateSchemeAnswers, ViewModelTransformer}
import org.scalatest.{Inspectors, Matchers}
import uk.gov.hmrc.play.test.UnitSpec

class AnnualCostsInclusiveSpec extends UnitSpec with Matchers with Inspectors with VatRegistrationFixture {

  private val validationFunction = AnnualCostsInclusiveView.valid

  "ViewModelTransformer" should {

    "update VatFlatRateSchemeAnswers with new AnnualCostsInclusiveView (answer YES)" in {
      val transformed = ViewModelTransformer[AnnualCostsInclusiveView, VatFlatRateScheme]
        .toApi(AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES), VatFlatRateScheme(annualCostsInclusive = Some(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS)))
      transformed.annualCostsInclusive shouldBe Some(AnnualCostsInclusiveView.YES)
    }

    "update VatFlatRateSchemeAnswers with new AnnualCostsInclusiveView (answer NO)" in {
      val transformed = ViewModelTransformer[AnnualCostsInclusiveView, VatFlatRateScheme]
        .toApi(AnnualCostsInclusiveView(AnnualCostsInclusiveView.NO), VatFlatRateScheme(annualCostsInclusive = Some(AnnualCostsInclusiveView.YES)))
      transformed.annualCostsInclusive shouldBe Some(AnnualCostsInclusiveView.NO)
    }

  }

  "ApiModelTransformer" should {

    "produce empty view model from an empty annual costs inclusive" in {
      val vm = ApiModelTransformer[AnnualCostsInclusiveView]
        .toViewModel(vatScheme(vatFlatRateSchemeAnswers = None))
      vm shouldBe None
    }

    "produce a view model from a vatScheme with annual costs inclusive (answer YES)" in {
      val vm = ApiModelTransformer[AnnualCostsInclusiveView]
        .toViewModel(vatScheme(vatFlatRateSchemeAnswers = Some(VatFlatRateScheme(annualCostsInclusive = Some(AnnualCostsInclusiveView.YES)))))
      vm shouldBe Some(AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES))
    }

    "produce a view model from a vatScheme with annual costs inclusive (answer NO)" in {
      val vm = ApiModelTransformer[AnnualCostsInclusiveView]
        .toViewModel(vatScheme(vatFlatRateSchemeAnswers = Some(VatFlatRateScheme(annualCostsInclusive = Some(AnnualCostsInclusiveView.NO)))))
      vm shouldBe Some(AnnualCostsInclusiveView(AnnualCostsInclusiveView.NO))
    }

    "produce a view model from a vatScheme with annual costs inclusive (answer YES within 12 months)" in {
      val vm = ApiModelTransformer[AnnualCostsInclusiveView]
        .toViewModel(vatScheme(vatFlatRateSchemeAnswers = Some(VatFlatRateScheme(annualCostsInclusive = Some(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS)))))
      vm shouldBe Some(AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS))
    }

  }

  "AnnualCostsInclusiveView is valid" when {

    "selected answer is one of the allowed values" in {
      forAll(Seq(AnnualCostsInclusiveView.YES, AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS, AnnualCostsInclusiveView.NO)) {
        validationFunction(_) shouldBe true
      }
    }

  }

  "AnnualCostsInclusiveView is NOT valid" when {

    "selected reason is not of the allowed values" in {
      forAll(Seq("", "not an allowed value")) {
        validationFunction(_) shouldBe false
      }
    }

  }

  "ViewModelFormat" should {
    val validAnnualCostsInclusiveView = AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS)
    val s4LTradingDetails: S4LFlatRateSchemeAnswers = S4LFlatRateSchemeAnswers(annualCostsInclusive = Some(validAnnualCostsInclusiveView))

    "extract annualCostsInclusive from vatTradingDetails" in {
      AnnualCostsInclusiveView.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validAnnualCostsInclusiveView)
    }

    "update empty vatFlatRateSchemeAnswers with annualCostsInclusive" in {
      AnnualCostsInclusiveView.viewModelFormat.update(validAnnualCostsInclusiveView, Option.empty[S4LFlatRateSchemeAnswers]).annualCostsInclusive shouldBe Some(validAnnualCostsInclusiveView)
    }

    "update non-empty vatFlatRateSchemeAnswers with annualCostsInclusive" in {
      AnnualCostsInclusiveView.viewModelFormat.update(validAnnualCostsInclusiveView, Some(s4LTradingDetails)).annualCostsInclusive shouldBe Some(validAnnualCostsInclusiveView)
    }

  }


}
