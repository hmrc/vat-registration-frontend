/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec

class CompanyContactDetailsSpec extends VatRegSpec {

  "apiReads" should {
    "parse the full json into a CompanyContactDetailsModel" in {

      val jsonToParse = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com",
          |   "tel" : "0123456",
          |   "mobile" : "987654"
          | },
          | "website" : "/test/url"
          |}
        """.stripMargin
      )

      val expectedModel = CompanyContactDetails(
        email          = "test@test.com",
        phoneNumber    = Some("0123456"),
        mobileNumber   = Some("987654"),
        websiteAddress = Some("/test/url")
      )


      Json.fromJson[CompanyContactDetails](jsonToParse)(CompanyContactDetails.apiReads) mustBe JsSuccess(expectedModel)
    }

    "parse minimal json into a CompanyContactDetailsModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com"
          | }
          |}
        """.stripMargin
      )

      val expectedModel = CompanyContactDetails(
        email          = "test@test.com",
        phoneNumber    = None,
        mobileNumber   = None,
        websiteAddress = None
      )


      Json.fromJson[CompanyContactDetails](jsonToParse)(CompanyContactDetails.apiReads) mustBe JsSuccess(expectedModel)
    }
  }

  "apiWrites" should {
    "transform a full CompanyContactDetails into a full set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com",
          |   "tel" : "0123456",
          |   "mobile" : "987654"
          | },
          | "website" : "/test/url"
          |}
        """.stripMargin
      )

      val modelToTransform = CompanyContactDetails(
        email          = "test@test.com",
        phoneNumber    = Some("0123456"),
        mobileNumber   = Some("987654"),
        websiteAddress = Some("/test/url")
      )

      Json.toJson(modelToTransform)(CompanyContactDetails.apiWrites) mustBe expectedJson
    }

    "transform a minimal CompanyContactDetails into a minimal set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com"
          | }
          |}
        """.stripMargin
      )

      val modelToTransform = CompanyContactDetails(
        email          = "test@test.com",
        phoneNumber    = None,
        mobileNumber   = None,
        websiteAddress = None
      )

      Json.toJson(modelToTransform)(CompanyContactDetails.apiWrites) mustBe expectedJson
    }
  }
}
