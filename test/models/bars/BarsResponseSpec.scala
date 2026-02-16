/*
 * Copyright 2026 HM Revenue & Customs
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

package models.bars

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class BarsResponseSpec extends AnyWordSpec with Matchers {

  "BarsResponse.format.reads" should {

    "parse known values case-insensitively" in {
      val cases = Seq(
        "yes"           -> BarsResponse.Yes,
        "no"            -> BarsResponse.No,
        "partial"       -> BarsResponse.Partial,
        "indeterminate" -> BarsResponse.Indeterminate,
        "inapplicable"  -> BarsResponse.Inapplicable,
        "error"         -> BarsResponse.Error
      )

      cases.foreach { case (str, expected) =>
        Json.fromJson[BarsResponse](JsString(str.toUpperCase)) shouldBe JsSuccess(expected)
        Json.fromJson[BarsResponse](JsString(str))           shouldBe JsSuccess(expected)
      }
    }

    "return JsError for unknown string values" in {
      Json.fromJson[BarsResponse](JsString("maybe")) shouldBe JsError("Unknown value: maybe")
    }

    "return JsError when JSON is not a string" in {
      Json.fromJson[BarsResponse](JsNumber(1)) shouldBe JsError("Expected String")
      Json.fromJson[BarsResponse](JsNull)      shouldBe JsError("Expected String")
      Json.fromJson[BarsResponse](Json.obj())  shouldBe JsError("Expected String")
    }
  }

  "BarsResponse.format.writes" should {

    "write lowercase string values" in {
      val cases = Seq(
        BarsResponse.Yes           -> "yes",
        BarsResponse.No            -> "no",
        BarsResponse.Partial       -> "partial",
        BarsResponse.Indeterminate -> "indeterminate",
        BarsResponse.Inapplicable  -> "inapplicable",
        BarsResponse.Error         -> "error"
      )

      cases.foreach { case (value, expected) =>
        Json.toJson(value: BarsResponse) shouldBe JsString(expected)
      }
    }
  }

  "BarsResponse.values" should {
    "contain all response variants" in {
      BarsResponse.values.toSet shouldBe Set(
        BarsResponse.Yes,
        BarsResponse.No,
        BarsResponse.Partial,
        BarsResponse.Indeterminate,
        BarsResponse.Inapplicable,
        BarsResponse.Error
      )
    }
  }
}