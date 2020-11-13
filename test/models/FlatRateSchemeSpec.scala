/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.libs.json.{Format, JsSuccess, JsValue, Json}
import testHelpers.VatRegSpec

class FlatRateSchemeSpec extends VatRegSpec {

  val validDate: LocalDate = LocalDate.now
  override val startDate: LocalDate = validDate

  implicit val frmt: Format[FlatRateScheme] = FlatRateScheme.apiFormat

  override val validFlatRate = FlatRateScheme(Some(true), Some(true), Some(13145L), Some(true), Some(true), Some(Start(Some(startDate))), Some("test"), Some(15.00), Some(false))

  val validFlateRateJoinFrsFalse = FlatRateScheme(Some(false), None, None, None, None, None, None, None, None)

  val validJson: JsValue    = Json.parse(
    s"""{
       |  "joinFrs" : true,
       |  "frsDetails" : {
       |    "businessGoods": {
       |      "estimatedTotalSales": 13145,
       |      "overTurnover"       : true
       |    },
       |    "startDate"         : "$validDate",
       |    "categoryOfBusiness": "test",
       |    "percent"           : 15.00,
       |    "limitedCostTrader" : false
       |  }
       |}""".stripMargin
  )

  "FlatRateScheme" should {
    "successfully construct json from the model" when {

      s"joinFrs is true and frsDetails defined with Business Goods - overBusinessGoods set to true" in {
        Json.toJson(validFlatRate.copy(overBusinessGoods = Some(true)))(FlatRateScheme.apiFormat) mustBe validJson
      }

      "joinFrs is true and frsDetails defined without Business Goods" in {
        val expectedJson = Json.parse(
          s"""{
             |  "joinFrs" : true,
             |  "frsDetails" : {
             |    "startDate": "$validDate",
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00,
             |    "limitedCostTrader" : false
             |  }
             |}""".stripMargin)

        Json.toJson(validFlatRate.copy(overBusinessGoods = Some(false)))(FlatRateScheme.apiFormat) mustBe expectedJson
      }

      "joinFrs is false and frsDetails defined" in {
        val expectedJson = Json.parse(
          s"""{
             |  "joinFrs" : false,
             |  "frsDetails" : {
             |    "businessGoods": {
             |      "estimatedTotalSales": 13145,
             |      "overTurnover"       : true
             |    },
             |    "startDate"         : "$validDate",
             |    "categoryOfBusiness": "test",
             |    "percent"           : 15.00,
             |    "limitedCostTrader" : false
             |  }
             |}""".stripMargin
        )

        Json.toJson(validFlatRate.copy(joinFrs = Some(false)))(FlatRateScheme.apiFormat) mustBe expectedJson
      }

      "joinFrs is false and frsDetails not defined" in {
        val expectedJson = Json.parse(
          s"""{
             |  "joinFrs" : false
             |}""".stripMargin
        )

        Json.toJson(validFlateRateJoinFrsFalse)(FlatRateScheme.apiFormat) mustBe expectedJson
      }
    }

    "Successfully construct a valid view model from API json" when {
      "joinFrs is true and Frs details is defined with Business Goods" in {
        val res = Json.fromJson[FlatRateScheme](validJson)(FlatRateScheme.apiFormat)
        res mustBe JsSuccess(validFlatRate.copy(overBusinessGoods = Some(true),
          overBusinessGoodsPercent = Some(true)))
      }

      "joinFrs is true and Frs details is defined without Business Goods" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : true,
             |  "frsDetails" : {
             |    "startDate": "$validDate",
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00,
             |    "limitedCostTrader" : false
             |  }
             |}""".stripMargin
        )

        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res mustBe JsSuccess(validFlatRate.copy(overBusinessGoods = Some(false), overBusinessGoodsPercent = None, estimateTotalSales = None))
      }

      "successfully populate startDate in view Model joinFrs true, startDate does not exist" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : true,
             |  "frsDetails" : {
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00,
             |    "limitedCostTrader" : false
             |  }
             |}""".stripMargin
        )
        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res mustBe JsSuccess(validFlatRate.copy(
          overBusinessGoods = Some(false),
          overBusinessGoodsPercent = None,
          estimateTotalSales = None,
          frsStart = Some(Start(None))))
      }

      "successfully populate startDate in view model join Frs false, startDate exists" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : false,
             |  "frsDetails" : {
             |    "startDate": "$validDate",
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00,
             |    "limitedCostTrader" : false
             |  }
             |}""".stripMargin
        )
        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res mustBe JsSuccess(validFlatRate.copy(
          overBusinessGoods = Some(false),
          overBusinessGoodsPercent = None,
          joinFrs = Some(false),
          useThisRate = Some(false),
          estimateTotalSales = None,
          frsStart = None))
      }

      "joinFrs is false" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : false
             |}""".stripMargin
        )
        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res mustBe JsSuccess(validFlateRateJoinFrsFalse)
      }
    }
  }
}
