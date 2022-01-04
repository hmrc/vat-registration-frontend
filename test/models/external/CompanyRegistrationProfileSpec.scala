/*
 * Copyright 2022 HM Revenue & Customs
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

package models.external

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class CompanyRegistrationProfileSpec extends PlaySpec {

  "CompanyRegistrationProfile" should {

    val incorpStatus = "acknowledged"
    val ctStatus = "04"

    "parse from json correctly" when {
      "json is full" in {
        val json = Json.obj(
          "status" -> incorpStatus,
          "ctStatus" -> ctStatus
        )
        val profile = CompanyRegistrationProfile(incorpStatus, Some(ctStatus))
        Json.fromJson[CompanyRegistrationProfile](json) mustBe JsSuccess(profile)
      }
      "json does not include a ctStatus" in {
        val json = Json.obj(
          "status" -> incorpStatus
        )
        val profile = CompanyRegistrationProfile(incorpStatus)
        Json.fromJson[CompanyRegistrationProfile](json) mustBe JsSuccess(profile)
      }
    }
    "fail to parse from json" when {
      "status is missing from the json" in {
        val json = Json.obj(
          "ctStatus" -> ctStatus
        )
        Json.fromJson[CompanyRegistrationProfile](json).isError mustBe true
      }
    }
  }
}
