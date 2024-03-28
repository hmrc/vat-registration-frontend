/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec

import java.time.LocalDateTime

class VatThresholdSpec extends VatRegSpec {

  val dateTime: LocalDateTime = LocalDateTime.of(2024, 4, 1, 0, 0, 0)
  val amount: BigDecimal = BigDecimal(90000)

  val model: VatThreshold = VatThreshold(dateTime, amount)

  val json: JsObject = Json.obj(
    "date" -> "2024-04-01T00:00:00",
    "amount" -> 90000
  )

  "VatThreshold" must {

    "Write to json" in {

      val expectedResult = json
      val actualResult = Json.toJson(model)

      expectedResult mustBe actualResult
    }

    "read from json" in {

      val expectedResult = model
      val actualResult = json.as[VatThreshold]

      expectedResult mustBe actualResult
    }
  }
}