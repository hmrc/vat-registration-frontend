/*
 * Copyright 2017 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import play.api.libs.json.{JsSuccess, JsValue, Json, __}
import uk.gov.hmrc.play.test.UnitSpec

class CompletionCapacitySpec extends UnitSpec with VatRegistrationFixture {


  "CompletionCapacity" should {
    "serialise to JSON" in {
      val bobJson = Json.parse(
        """{
          |    "name" : {
          |        "forename" : "Bob",
          |        "other_forenames" : "Bimbly Bobblous",
          |        "surname" : "Bobbings"
          |    },
          |    "role" : "director"
          |}""".stripMargin)

      JsSuccess(completionCapacity) shouldBe CompletionCapacity.format.reads(bobJson)
    }
  }

  "CompletionCapacity" should {
    "have equality equal" in {
      val completionCapacity1 = CompletionCapacity(name = Name(Some("forename"), Some("other names"), "surname"), role = "director")
      val completionCapacity2 = CompletionCapacity(name = Name(Some("forename"), Some("other names"), "surname"), role = "director")

      (completionCapacity1 == completionCapacity2) shouldBe true
    }
  }

  "CompletionCapacity" should {
    "have equality not-equal" in {
      val completionCapacity1 = CompletionCapacity(name = Name(Some("forename"), Some("other names"), "surname"), role = "director")
      val completionCapacity2 = CompletionCapacity(name = Name(Some("forename"), None, "surname"), role = "director")

      (completionCapacity1 == completionCapacity2) shouldBe false
    }
  }

}
