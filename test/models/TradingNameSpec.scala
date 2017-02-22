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

package models

import fixtures.VatRegistrationFixture
import models.api.{VatScheme, VatTradingDetails}
import models.view.TradingName
import models.view.TradingName._
import uk.gov.hmrc.play.test.UnitSpec

class TradingNameSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty TradingName model" in {
      TradingName.empty shouldBe TradingName("", None)
    }
  }

  "toString" should {
    "TradingName with a trading name returns it when toString is called" in {
      TradingName("", Some("Test Ltd")).toString shouldBe "Test Ltd"
    }

    "TradingName with an empty trading name returns empty string" in {
      TradingName("", None).toString shouldBe ""
    }
  }

  "toApi" should {
    "update a VatTradingDetails a new TradingName" in {
      val tradingName = TradingName(TradingName.TRADING_NAME_YES, Some("HOLIDAY INC"))
      tradingName.toApi(validVatTradingDetails) shouldBe VatTradingDetails("HOLIDAY INC")
    }
  }

  "apply" should {
    "extract a TradingName from a VatScheme" in {
      TradingName.apply(validVatScheme) shouldBe validTradingName
    }

    "convert a populated VatScheme's VatTradingDetails API model that has an empty trading name to an instance of TradingName view model" in {
      val vatSchemeEmptyTradingName = VatScheme(
        id = validRegId,
        tradingDetails = Some(VatTradingDetails("")))

      TradingName.apply(vatSchemeEmptyTradingName) shouldBe TradingName(yesNo = TRADING_NAME_NO, tradingName = None)
    }

  }
}
