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

package models.view.vatTradingDetails

import fixtures.VatRegistrationFixture
import models.api.VatScheme
import models.view.vatTradingDetails.TradingNameView._
import models.{ApiModelTransformer, S4LTradingDetails}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class TradingNameViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "apply" should {
    "extract a TradingNameView from a VatScheme" in {
      val vm = ApiModelTransformer[TradingNameView].toViewModel(validVatScheme)
      vm shouldBe Some(TradingNameView(TradingNameView.TRADING_NAME_YES, tradingName = Some(testTradingName)))
    }

    "extract a TradingNameView from VatScheme with no trading name returns empty trading name" in {
      val vatSchemeEmptyTradingName = VatScheme(id = testRegId, tradingDetails = Some(tradingDetails(tradingName = None)))
      val expectedVM = Some(TradingNameView(yesNo = TRADING_NAME_NO, tradingName = None))
      ApiModelTransformer[TradingNameView].toViewModel(vatSchemeEmptyTradingName) shouldBe expectedVM
    }

    "extract a TradingNameView from VatScheme with no VatTradingDetails returns empty trading name" in {
      val vatSchemeEmptyTradingDetails = VatScheme(id = testRegId, tradingDetails = None)
      ApiModelTransformer[TradingNameView].toViewModel(vatSchemeEmptyTradingDetails) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val s4LTradingDetails: S4LTradingDetails = S4LTradingDetails(tradingName = Some(validTradingNameView))

    "extract tradingName from vatTradingDetails" in {
      TradingNameView.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validTradingNameView)
    }

    "update empty vatContact with tradingName" in {
      TradingNameView.viewModelFormat.update(validTradingNameView, Option.empty[S4LTradingDetails]).tradingName shouldBe Some(validTradingNameView)
    }

    "update non-empty vatContact with tradingName" in {
      TradingNameView.viewModelFormat.update(validTradingNameView, Some(s4LTradingDetails)).tradingName shouldBe Some(validTradingNameView)
    }

  }
}
