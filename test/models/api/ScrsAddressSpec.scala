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

package models.api

import org.scalatest.Inspectors
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec


class ScrsAddressSpec extends UnitSpec with Inspectors {


  "ScrsAddress" should {

    "read from valid complete Json" in {

      val validJson = Json.parse(
        """{
          | "address": {
          |   "lines": ["line 1", "line 2", "line 3", "line 4"],
          |   "postcode": "BN3 1JU",
          |   "country": {
          |     "code": "UK"
          |   }
          | }
          |}""".stripMargin)

      implicit val alReads = ScrsAddress.adressLookupReads
      validJson.validate[ScrsAddress] shouldBe JsSuccess(
        ScrsAddress(
          line1 = "line 1",
          line2 = "line 2",
          line3 = Some("line 3"),
          line4 = Some("line 4"),
          country = None,
          postcode = Some("BN3 1JU")
        ))
    }


    "read from valid minimal Json - no country information" in {

      val validJson = Json.parse(
        """{
          | "address": {
          |   "lines": ["line 1", "line 2"],
          |   "postcode": "BN3 1JU"
          | }
          |}""".stripMargin)

      implicit val alReads = ScrsAddress.adressLookupReads
      validJson.validate[ScrsAddress] shouldBe JsSuccess(ScrsAddress("line 1", "line 2", postcode = Some("BN3 1JU")))
    }


    "read from valid minimal Json - no postcode" in {

      val validJson = Json.parse(
        """{
          | "address": {
          |   "lines": ["line 1", "line 2"],
          |   "country": {
          |     "code": "UK"
          |   }
          | }
          |}""".stripMargin)

      implicit val alReads = ScrsAddress.adressLookupReads
      validJson.validate[ScrsAddress] shouldBe JsSuccess(ScrsAddress("line 1", "line 2", country = Some("UK")))
    }

    "read from valid minimal Json - no postcode - country name present" in {
      val validJson = Json.parse(
        """{
          | "address": {
          |   "lines": ["line 1", "line 2"],
          |   "country": {
          |     "name": "United Kingdom",
          |     "code": "UK"
          |   }
          | }
          |}""".stripMargin)

      implicit val alReads = ScrsAddress.adressLookupReads
      validJson.validate[ScrsAddress] shouldBe JsSuccess(ScrsAddress("line 1", "line 2", country = Some("United Kingdom")))
    }

  }

}
