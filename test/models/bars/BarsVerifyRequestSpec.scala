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
import play.api.libs.json.{JsSuccess, Json}

class BarsVerifyRequestSpec extends AnyWordSpec with Matchers {

  "BarsAccount" should {

    val model = BarsAccount(sortCode = "123456", accountNumber = "12345678")
    val json  = Json.parse("""{"sortCode":"123456","accountNumber":"12345678"}""")

    "serialise to JSON correctly" in {
      Json.toJson(model) shouldBe json
    }

    "deserialise from JSON correctly" in {
      json.validate[BarsAccount] shouldBe JsSuccess(model)
    }

    "fail to deserialise when a required field is missing" in {
      val incomplete = Json.parse("""{"sortCode":"123456"}""")
      incomplete.validate[BarsAccount].isError shouldBe true
    }
  }

  "BarsSubject" should {

    val model = BarsSubject(name = "Jane Doe")
    val json  = Json.parse("""{"name":"Jane Doe"}""")

    "serialise to JSON correctly" in {
      Json.toJson(model) shouldBe json
    }

    "deserialise from JSON correctly" in {
      json.validate[BarsSubject] shouldBe JsSuccess(model)
    }

    "fail to deserialise when a required field is missing" in {
      Json.obj().validate[BarsSubject].isError shouldBe true
    }
  }

  "BarsBusiness" should {

    val model = BarsBusiness(companyName = "Acme Corp")
    val json  = Json.parse("""{"companyName":"Acme Corp"}""")

    "serialise to JSON correctly" in {
      Json.toJson(model) shouldBe json
    }

    "deserialise from JSON correctly" in {
      json.validate[BarsBusiness] shouldBe JsSuccess(model)
    }

    "fail to deserialise when a required field is missing" in {
      Json.obj().validate[BarsBusiness].isError shouldBe true
    }
  }

  "BarsPersonalRequest" should {

    val model = BarsPersonalRequest(
      account = BarsAccount(sortCode = "123456", accountNumber = "12345678"),
      subject = BarsSubject(name = "Jane Doe")
    )

    val json = Json.parse(
      """{
        |  "account": {"sortCode":"123456","accountNumber":"12345678"},
        |  "subject": {"name":"Jane Doe"}
        |}""".stripMargin
    )

    "serialise to JSON correctly" in {
      Json.toJson(model) shouldBe json
    }

    "deserialise from JSON correctly" in {
      json.validate[BarsPersonalRequest] shouldBe JsSuccess(model)
    }

    "fail to deserialise when the account field is missing" in {
      val incomplete = Json.parse("""{"subject":{"name":"Jane Doe"}}""")
      incomplete.validate[BarsPersonalRequest].isError shouldBe true
    }

    "fail to deserialise when the subject field is missing" in {
      val incomplete = Json.parse("""{"account":{"sortCode":"123456","accountNumber":"12345678"}}""")
      incomplete.validate[BarsPersonalRequest].isError shouldBe true
    }
  }

  "BarsBusinessRequest" should {

    val model = BarsBusinessRequest(
      account  = BarsAccount(sortCode = "123456", accountNumber = "12345678"),
      business = BarsBusiness(companyName = "Acme Corp")
    )

    val json = Json.parse(
      """{
        |  "account":  {"sortCode":"123456","accountNumber":"12345678"},
        |  "business": {"companyName":"Acme Corp"}
        |}""".stripMargin
    )

    "serialise to JSON correctly" in {
      Json.toJson(model) shouldBe json
    }

    "deserialise from JSON correctly" in {
      json.validate[BarsBusinessRequest] shouldBe JsSuccess(model)
    }

    "fail to deserialise when the account field is missing" in {
      val incomplete = Json.parse("""{"business":{"companyName":"Acme Corp"}}""")
      incomplete.validate[BarsBusinessRequest].isError shouldBe true
    }

    "fail to deserialise when the business field is missing" in {
      val incomplete = Json.parse("""{"account":{"sortCode":"123456","accountNumber":"12345678"}}""")
      incomplete.validate[BarsBusinessRequest].isError shouldBe true
    }
  }
}