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

import play.api.libs.json.Json
import testHelpers.VatRegSpec

class IncorporationDetailsSpec extends VatRegSpec {

  "IncorporationDetails" should {
    "parse successfully without optional data" in {
      val incorpDetails = testIncorpDetails.copy(businessVerification = None, bpSafeId = None)
      val json = Json.toJson(incorpDetails)
      json.as[IncorporationDetails] mustBe incorpDetails
    }

    "parse successfuly with optional data" in {
      val incorpDetails = testIncorpDetails
      val json = Json.toJson(incorpDetails)
      json.as[IncorporationDetails] mustBe incorpDetails
    }
  }

  "IncorporationDetails apiFormat" should {
    "parse successfully without optional data" in {
      val incorpDetails = testIncorpDetails.copy(businessVerification = None, bpSafeId = None)
      val json = Json.toJson(incorpDetails)(IncorporationDetails.apiFormat)
      json.as[IncorporationDetails](IncorporationDetails.apiFormat) mustBe incorpDetails
    }

    "parse successfuly with optional data" in {
      val incorpDetails = testIncorpDetails
      val json = Json.toJson(incorpDetails)(IncorporationDetails.apiFormat)
      json.as[IncorporationDetails](IncorporationDetails.apiFormat) mustBe incorpDetails
    }
  }

}
