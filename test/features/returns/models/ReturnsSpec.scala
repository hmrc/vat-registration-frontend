/*
 * Copyright 2019 HM Revenue & Customs
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

package features.returns.models

import java.time.LocalDate

import features.returns.models.Start
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ReturnsSpec extends UnitSpec {

  val validDate = LocalDate.now

  val reclaimOnReturns = true
  val returnsFrequency = Frequency.monthly
  val startDate        = validDate

  val validReturns = Returns(Some(reclaimOnReturns), Some(returnsFrequency), Some(Stagger.feb), Some(Start(Some(startDate))))
  val validJson    = Json.parse(
    s"""{
       |  "reclaimVatOnMostReturns" : true,
       |  "frequency" : "monthly",
       |  "staggerStart" : "feb",
       |  "start" : {
       |    "date" : "$validDate"
       |  }
       |}""".stripMargin
  )

  "Returns" should {
    "construct valid Json from the model" in {
      Json.toJson[Returns](validReturns) shouldBe validJson
    }
    "construct a valid model from the Json" in {
      Json.fromJson[Returns](validJson) shouldBe JsSuccess(validReturns)
    }
  }

  "empty" should {
    "construct an empty Returns model" in {
      Returns.empty shouldBe Returns(None, None, None, None)
    }
  }
}
