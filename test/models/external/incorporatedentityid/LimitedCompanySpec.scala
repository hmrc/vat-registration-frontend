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

package models.external.incorporatedentityid

import models.external.LimitedCompany
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class LimitedCompanySpec extends VatRegSpec {

  "LimitedCompany" should {
    "parse successfully without optional data" in {
      val incorpDetails = testLimitedCompany.copy(businessVerification = None, bpSafeId = None)
      val json = Json.toJson(incorpDetails)
      json.as[LimitedCompany] mustBe incorpDetails
    }

    "parse successfully with optional data" in {
      val incorpDetails = testLimitedCompany
      val json = Json.toJson(incorpDetails)
      json.as[LimitedCompany] mustBe incorpDetails
    }
  }

  "LimitedCompany apiFormat" should {
    "parse successfully without optional data" in {
      val incorpDetails = testLimitedCompany.copy(businessVerification = None, bpSafeId = None)
      val json = Json.toJson(incorpDetails)(LimitedCompany.apiFormat)
      json.as[LimitedCompany](LimitedCompany.apiFormat) mustBe incorpDetails
    }

    "parse successfully with optional data" in {
      val incorpDetails = testLimitedCompany
      val json = Json.toJson(incorpDetails)(LimitedCompany.apiFormat)
      json.as[LimitedCompany](LimitedCompany.apiFormat) mustBe incorpDetails
    }
  }

}
