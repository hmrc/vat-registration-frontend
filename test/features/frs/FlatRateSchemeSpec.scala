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

package features.frs

import java.time.LocalDate

import features.returns.Start
import frs.{AnnualCosts, FlatRateScheme}
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class FlatRateSchemeSpec extends UnitSpec {
  val validDate = LocalDate.now
  val startDate = validDate

  implicit val frmt = FlatRateScheme.apiFormat

  val validFlatRate = FlatRateScheme(Some(true), Some(AnnualCosts.WillSpend), Some(13145L), Some(AnnualCosts.WillSpend), Some(true), Some(Start(Some(startDate))), Some("test"), Some(15.00))

  val validFlateRateJoinFrsFalse = FlatRateScheme(Some(false), None, None,None,None,None,None, None)

  val validJson    = Json.parse(
    s"""{
       |  "joinFrs" : true,
       |  "frsDetails" : {
       |    "businessGoods": {
       |      "estimatedTotalSales": 13145,
       |      "overTurnover"       : true
       |    },
       |    "startDate"         : "$validDate",
       |    "categoryOfBusiness": "test",
       |    "percent"           : 15.00
       |  }
       |}""".stripMargin
  )

  "FlatRateScheme" should {
    "successfully construct json from the model" when {
      s"joinFrs is true and frsDetails defined with Business Goods - overBusinessGoods set to ${AnnualCosts.WillSpend}" in {
        Json.toJson(validFlatRate)(FlatRateScheme.apiFormat) shouldBe validJson
      }

      s"joinFrs is true and frsDetails defined with Business Goods - overBusinessGoods set to ${AnnualCosts.AlreadyDoesSpend}" in {
        Json.toJson(validFlatRate.copy(overBusinessGoods = Some(AnnualCosts.AlreadyDoesSpend)))(FlatRateScheme.apiFormat) shouldBe validJson
      }

      "joinFrs is true and frsDetails defined without Business Goods" in {
        val expectedJson = Json.parse(
          s"""{
             |  "joinFrs" : true,
             |  "frsDetails" : {
             |    "startDate": "$validDate",
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00
             |  }
             |}""".stripMargin)

        Json.toJson(validFlatRate.copy(overBusinessGoods = Some(AnnualCosts.DoesNotSpend)))(FlatRateScheme.apiFormat) shouldBe expectedJson
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
             |    "percent"           : 15.00
             |  }
             |}""".stripMargin
        )

        Json.toJson(validFlatRate.copy(joinFrs = Some(false)))(FlatRateScheme.apiFormat) shouldBe expectedJson
      }

      "joinFrs is false and frsDetails not defined" in {
        Json.toJson(validFlatRate)(FlatRateScheme.apiFormat) shouldBe validJson
      }
    }

    "Successfully construct a valid view model from API json" when {
      "joinFrs is true and Frs details is defined with Business Goods" in {
        val res = Json.fromJson[FlatRateScheme](validJson)(FlatRateScheme.apiFormat)
        res shouldBe JsSuccess(validFlatRate.copy(overBusinessGoods = Some(AnnualCosts.AlreadyDoesSpend),
          overBusinessGoodsPercent = Some(AnnualCosts.AlreadyDoesSpend)))
      }

      "joinFrs is true and Frs details is defined without Business Goods" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : true,
             |  "frsDetails" : {
             |    "startDate": "$validDate",
             |    "categoryOfBusiness" : "test",
             |    "percent" : 15.00
             |  }
             |}""".stripMargin
        )

        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res shouldBe JsSuccess(validFlatRate.copy(overBusinessGoods = Some(AnnualCosts.DoesNotSpend), overBusinessGoodsPercent = None, estimateTotalSales = None))
      }

      "joinFrs is false" in {
        val json = Json.parse(
          s"""{
             |  "joinFrs" : false
             |}""".stripMargin
        )
        val res = Json.fromJson[FlatRateScheme](json)(FlatRateScheme.apiFormat)
        res shouldBe JsSuccess(validFlateRateJoinFrsFalse)
      }
    }
  }
}
