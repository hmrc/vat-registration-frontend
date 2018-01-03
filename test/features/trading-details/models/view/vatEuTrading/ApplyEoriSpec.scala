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

package models.view.vatTradingDetails.vatEuTrading

import fixtures.VatRegistrationFixture
import models.{ApiModelTransformer, S4LTradingDetails}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class ApplyEoriSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "apply" should {

    "convert VatScheme without VatTradingDetails to empty view model" in {
      val vs = vatScheme(vatTradingDetails = None)
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with VatEuTrading section to view model - ApplyEori Yes" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(eoriApplication = Some(true))))
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe Some(ApplyEori(ApplyEori.APPLY_EORI_YES))
    }

    "convert VatScheme with VatEuTrading section to view model - ApplyEori No" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(eoriApplication = Some(false))))
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe Some(ApplyEori(ApplyEori.APPLY_EORI_NO))
    }

  }

  "ViewModelFormat" should {
    val s4LTradingDetails: S4LTradingDetails = S4LTradingDetails(applyEori = Some(validApplyEori))

    "extract applyEori from vatTradingDetails" in {
      ApplyEori.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validApplyEori)
    }

    "update empty vatContact with applyEori" in {
      ApplyEori.viewModelFormat.update(validApplyEori, Option.empty[S4LTradingDetails]).applyEori shouldBe Some(validApplyEori)
    }

    "update non-empty vatContact with applyEori" in {
      ApplyEori.viewModelFormat.update(validApplyEori, Some(s4LTradingDetails)).applyEori shouldBe Some(validApplyEori)
    }

  }
  
}
