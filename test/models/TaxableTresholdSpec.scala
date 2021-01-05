/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}

class TaxableThresholdSpec extends PlaySpec {

  val json: JsValue = Json.parse(
    s"""
      |{
      |  "taxable-threshold":"50000",
      |  "since":"2018-01-01"
      |}
      |""".stripMargin)

  val invalidJson: JsValue = Json.parse(
    s"""
      |{
      |  "taxable-threshold":100,
      |  "since":"2018-01-01"
      |}
    """.stripMargin
  )

  val model = TaxableThreshold("50000", "2018-01-01")

  "TaxableThreshold" must {
    "read json into a model" in {
      val covertedModel = Json.fromJson[TaxableThreshold](json)
      covertedModel.isSuccess mustBe true
      covertedModel.get mustBe model
    }

    "fail to read json into a model if the json is incorrect" in {
      val convertedModel = Json.fromJson[TaxableThreshold](invalidJson)
      convertedModel.isError mustBe true
    }

    "write a model into json" in {
      val convertedJson = Json.toJson[TaxableThreshold](model)
      convertedJson mustBe json
    }
  }
}
