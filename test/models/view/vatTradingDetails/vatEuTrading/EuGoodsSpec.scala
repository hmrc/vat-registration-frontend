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

package models.view.vatTradingDetails.vatEuTrading

import fixtures.VatRegistrationFixture
import models.api.{VatEuTrading, VatTradingDetails}
import models.{ApiModelTransformer, S4LTradingDetails, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class EuGoodsSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "toApi" should {
    val euGoods = EuGoods(EuGoods.EU_GOODS_YES)

    val differentVatTradingDetails = VatTradingDetails(
      validVatChoice,
      validTradingName,
      VatEuTrading(true, None)
    )

    "update VatTradingDetails with new EuGoods" in {
      ViewModelTransformer[EuGoods, VatTradingDetails]
        .toApi(euGoods, validVatTradingDetails) shouldBe differentVatTradingDetails
    }
  }

  "apply" should {

    "convert VatScheme without VatTradingDetails to empty view model" in {
      val vs = vatScheme(vatTradingDetails = None)
      ApiModelTransformer[EuGoods].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with VatEuTrading section to view model - EuGoods Yes" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(euGoodsSelection = true)))
      ApiModelTransformer[EuGoods].toViewModel(vs) shouldBe Some(EuGoods(EuGoods.EU_GOODS_YES))
    }

    "convert VatScheme with VatEuTrading section to view model - EuGoods No" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(euGoodsSelection = false)))
      ApiModelTransformer[EuGoods].toViewModel(vs) shouldBe Some(EuGoods(EuGoods.EU_GOODS_NO))
    }

  }

  "ViewModelFormat" should {
    val s4LTradingDetails: S4LTradingDetails = S4LTradingDetails(euGoods = Some(validEuGoods))

    "extract euGoods from vatTradingDetails" in {
      EuGoods.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validEuGoods)
    }

    "update empty vatContact with euGoods" in {
      EuGoods.viewModelFormat.update(validEuGoods, Option.empty[S4LTradingDetails]).euGoods shouldBe Some(validEuGoods)
    }

    "update non-empty vatContact with euGoods" in {
      EuGoods.viewModelFormat.update(validEuGoods, Some(s4LTradingDetails)).euGoods shouldBe Some(validEuGoods)
    }

  }
  
}

