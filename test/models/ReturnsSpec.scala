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

import java.time.LocalDate

import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec

class ReturnsSpec extends VatRegSpec {

  val validDate = LocalDate.now

  override val reclaimOnReturns = true
  override val returnsFrequency = Frequency.monthly
  override val startDate        = validDate

  override val validReturns = Returns(Some(10000.5), Some(reclaimOnReturns), Some(returnsFrequency), Some(Stagger.feb), Some(Start(Some(startDate))))
  val validJson    = Json.parse(
    s"""{
       |  "zeroRatedSupplies": 10000.5,
       |  "reclaimVatOnMostReturns": true,
       |  "frequency": "monthly",
       |  "staggerStart": "feb",
       |  "start": {
       |    "date": "$validDate"
       |  }
       |}""".stripMargin
  )

  "Returns" should {
    "construct valid Json from the model" in {
      Json.toJson[Returns](validReturns) mustBe validJson
    }
    "construct a valid model from the Json" in {
      Json.fromJson[Returns](validJson) mustBe JsSuccess(validReturns)
    }
  }

  "empty" should {
    "construct an empty Returns model" in {
      Returns.empty mustBe Returns(None, None, None, None, None)
    }
  }
}
