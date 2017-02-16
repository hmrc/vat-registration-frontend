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
import models.view.{EstimateVatTurnover, TradingName}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateVatTurnoverSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty EstimateVatTurnover model" in {
      EstimateVatTurnover.empty shouldBe EstimateVatTurnover(None)
    }
  }

  "toApi" should {
    "upserts (merge) a current VatFinancials API model with the details of an instance of EstimateVatTurnover view model" in {
      differentEstimateVatTurnover.toApi(validVatFinancials) shouldBe differentVatFinancials
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatFinancials API model to an instance of EstimateVatTurnover view model" in {
      EstimateVatTurnover.apply(validVatScheme) shouldBe validEstimateVatTurnover
    }
  }
}
