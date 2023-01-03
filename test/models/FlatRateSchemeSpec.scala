/*
 * Copyright 2023 HM Revenue & Customs
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

import fixtures.FlatRateFixtures
import play.api.libs.json.{JsObject, JsSuccess, Json}
import testHelpers.VatRegSpec

class FlatRateSchemeSpec extends VatRegSpec with FlatRateFixtures {

  val completeFlatRateSchemeJson: JsObject = Json.obj(
    "joinFrs" -> true,
    "overBusinessGoods" -> true,
    "estimateTotalSales" -> BigDecimal(5003),
    "overBusinessGoodsPercent" -> true,
    "useThisRate" -> true,
    "frsStart" -> frsDate,
    "categoryOfBusiness" -> testBusinessCategory,
    "percent" -> flatRatePercentage,
    "limitedCostTrader" -> false
  )

  val incompleteFlatRateSchemeJson: JsObject = Json.obj(
    "joinFrs" -> true,
    "overBusinessGoods" -> true
  )

  "Creating a FlatRateScheme model from Json" should {
    "complete successfully" when {
      "complete json provided" in {
        Json.fromJson[FlatRateScheme](completeFlatRateSchemeJson) mustBe JsSuccess(validFlatRate)
      }
      "incomplete json provided" in {
        Json.fromJson[FlatRateScheme](incompleteFlatRateSchemeJson) mustBe JsSuccess(incompleteFlatRate)
      }
    }
  }

  "Parsing FlatRateScheme to Json" should {
    "succeed" when {
      "FlatRateScheme is full" in {
        Json.toJson[FlatRateScheme](validFlatRate) mustBe completeFlatRateSchemeJson
      }
      "FlatRateScheme is incomplete" in {
        Json.toJson[FlatRateScheme](incompleteFlatRate) mustBe incompleteFlatRateSchemeJson
      }
    }
  }
}
