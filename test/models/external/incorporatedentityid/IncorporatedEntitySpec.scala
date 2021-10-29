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

import models.external.{BvPass, IncorporatedEntity}
import play.api.libs.json.{JsObject, JsString, JsSuccess, Json}
import testHelpers.VatRegSpec

class IncorporatedEntitySpec extends VatRegSpec {

  "LimitedCompany" should {
    "parse successfully without optional data" in {
      val incorpDetails = testLimitedCompany.copy(bpSafeId = None)
      val json = Json.toJson(incorpDetails)
      json.as[IncorporatedEntity] mustBe incorpDetails
    }

    "parse successfully with optional data" in {
      val incorpDetails = testLimitedCompany
      val json = Json.toJson(incorpDetails)
      json.as[IncorporatedEntity] mustBe incorpDetails
    }

    "parse successfully with an empty dateOfIncorporation" in {
      val testIncorpNoRegDate: JsObject =
        Json.obj("companyProfile" ->
          Json.obj(
            "companyNumber" -> testCrn,
            "companyName" -> testCompanyName
          ),
          "dateOfIncorporation" -> JsString(""),
          "countryOfIncorporation" -> testCountry,
          "identifiersMatch" -> true,
          "registration" ->
            Json.obj(
              "registrationStatus" -> "REGISTERED",
              "registeredBusinessPartnerId" -> testBpSafeId
            ),
          "businessVerification" ->
            Json.obj(
              "verificationStatus" -> "PASS"
            )
        )

      val res = Json.fromJson[IncorporatedEntity](testIncorpNoRegDate)(IncorporatedEntity.apiFormat)

      val expected: IncorporatedEntity = IncorporatedEntity(
        companyNumber = testCrn,
        companyName = Some(testCompanyName),
        ctutr = None,
        chrn = None,
        dateOfIncorporation = None,
        countryOfIncorporation = "GB",
        identifiersMatch = true,
        registration = testRegistration,
        businessVerification = BvPass,
        bpSafeId = Some(testBpSafeId)
      )

      res mustBe JsSuccess(expected)

    }
  }

  "LimitedCompany apiFormat" should {
    "parse successfully without optional data" in {
      val incorpDetails = testLimitedCompany.copy(bpSafeId = None)
      val json = Json.toJson(incorpDetails)(IncorporatedEntity.apiFormat)
      json.as[IncorporatedEntity](IncorporatedEntity.apiFormat) mustBe incorpDetails
    }

    "parse successfully with optional data" in {
      val incorpDetails = testLimitedCompany
      val json = Json.toJson(incorpDetails)(IncorporatedEntity.apiFormat)
      json.as[IncorporatedEntity](IncorporatedEntity.apiFormat) mustBe incorpDetails
    }
  }

}
