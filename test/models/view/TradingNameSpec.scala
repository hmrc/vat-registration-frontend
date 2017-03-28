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

package models.view

import fixtures.VatRegistrationFixture
import models.api.{VatScheme, VatTradingDetails}
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.TradingNameView._
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class TradingNameSpec extends UnitSpec with VatRegistrationFixture {

  "toString" should {
    "TradingNameView with a trading name returns it when toString is called" in {
      TradingNameView("", Some("Test Ltd")).toString shouldBe "Test Ltd"
    }

    "TradingNameView with an empty trading name returns empty string" in {
      TradingNameView("", None).toString shouldBe ""
    }
  }

  "toApi" should {
    "update a VatTradingDetails a new TradingNameView" in {
      val tradingName = TradingNameView(TradingNameView.TRADING_NAME_YES, Some("HOLIDAY INC"))
      ViewModelTransformer[TradingNameView, VatTradingDetails]
        .toApi(tradingName, validVatTradingDetails) shouldBe VatTradingDetails("HOLIDAY INC")
    }
  }

  "apply" should {
    "extract a TradingNameView from a VatScheme" in {
      ApiModelTransformer[TradingNameView].toViewModel(validVatScheme) shouldBe Some(validTradingName)
    }

    "extract a TradingNameView from VatScheme with no trading name returns empty trading name" in {
      val vatSchemeEmptyTradingName = VatScheme(id = validRegId, tradingDetails = Some(VatTradingDetails()))
      ApiModelTransformer[TradingNameView].toViewModel(vatSchemeEmptyTradingName) shouldBe Some(TradingNameView(yesNo = TRADING_NAME_NO, tradingName = None))
    }

    "extract a TradingNameView from VatScheme with no VatTradingDetails returns empty trading name" in {
      val vatSchemeEmptyTradingDetails = VatScheme(id = validRegId, tradingDetails = None)
      ApiModelTransformer[TradingNameView].toViewModel(vatSchemeEmptyTradingDetails) shouldBe None
    }

  }
}
