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
import models.view.TradingName
import uk.gov.hmrc.play.test.UnitSpec

class TradingNameSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty TradingName model" in {
      TradingName.empty shouldBe TradingName("", None)
    }
  }

  "toApi" should {
    "upserts (merge) a current VatTradingDetails API model with the details of an instance of TradingName view model" in {
      differentTradingName.toApi(validVatTradingDetails) shouldBe differentVatTradingDetails
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatTradingDetails API model to an instance of TradingName view model" in {
      TradingName.apply(validVatScheme) shouldBe validTradingName
    }
  }
}
