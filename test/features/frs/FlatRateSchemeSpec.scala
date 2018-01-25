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

  val validFlatRate = FlatRateScheme(Some(true), Some(AnnualCosts.DoesNotSpend), Some(13145L), Some(AnnualCosts.WillSpend), Some(true), Some(Start(Some(startDate))), Some(""), Some(15))
  val validJson    = Json.parse(
    s"""{
       |  "joinFrs" : true,
       |  "frsDetails" : {
       |    "overBusinessGoods" : false,
       |    "overBusinessGoodsPercent" : true,
       |    "vatInclusiveTurnover" : 13145,
       |    "start" : {
       |      "date" : "$validDate"
       |    },
       |    "categoryOfBusiness" : "",
       |    "percent" : 15
       |  }
       |}""".stripMargin
  )

  "FlatRateScheme" should {
    "construct valid Json from the model" in {
      Json.toJson(validFlatRate) shouldBe validJson
    }
    "construct a valid model from the Json" in {
      Json.fromJson[FlatRateScheme](validJson) shouldBe JsSuccess(validFlatRate.copy(overBusinessGoodsPercent = Some(AnnualCosts.AlreadyDoesSpend)))
    }
  }

  "empty" should {
    "construct an empty FlatRateScheme model" in {
      FlatRateScheme.empty shouldBe FlatRateScheme()
    }
  }
}
