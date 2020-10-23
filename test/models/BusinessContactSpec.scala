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

import models.api.Address
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class BusinessContactSpec extends VatRegSpec {

  "when parsing json from the api" should {
    "parse the full json into a BusinessContactModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com",
          |   "tel" : "0123456",
          |   "mobile" : "987654"
          | },
          | "website" : "/test/url",
          | "ppob" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "line3" : "testLine3",
          |   "line4" : "testLine4",
          |   "postcode" : "TE57 7ET"
          | },
          | "contactPreference": "Email"
          |}
        """.stripMargin
      )

      val expectedModel = BusinessContact(
        ppobAddress = Some(Address(
          line1    = "testLine1",
          line2    = "testLine2",
          line3    = Some("testLine3"),
          line4    = Some("testLine4"),
          postcode = Some("TE57 7ET")
        )),
        companyContactDetails = Some(CompanyContactDetails(
          email          = "test@test.com",
          phoneNumber    = Some("0123456"),
          mobileNumber   = Some("987654"),
          websiteAddress = Some("/test/url")
        )),
        contactPreference = Some(Email)
      )

      jsonToParse.as[BusinessContact](BusinessContact.apiFormat) mustBe expectedModel
    }

    "parse the minimal json into a BusinessContactModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          |}
        """.stripMargin
      )

      val expectedModel = BusinessContact()

      jsonToParse.as[BusinessContact](BusinessContact.apiFormat) mustBe expectedModel
    }
  }

  "when converting the model to json for the API" should {
    "transform a full BusinessContact into a full set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com",
          |   "tel" : "0123456",
          |   "mobile" : "987654"
          | },
          | "website" : "/test/url",
          | "ppob" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "line3" : "testLine3",
          |   "line4" : "testLine4",
          |   "postcode" : "TE57 7ET"
          | },
          | "contactPreference": "Letter"
          |}
        """.stripMargin
      )

      val modelToTransform = BusinessContact(
        ppobAddress = Some(Address(
          line1    = "testLine1",
          line2    = "testLine2",
          line3    = Some("testLine3"),
          line4    = Some("testLine4"),
          postcode = Some("TE57 7ET")
        )),
        companyContactDetails = Some(CompanyContactDetails(
          email          = "test@test.com",
          phoneNumber    = Some("0123456"),
          mobileNumber   = Some("987654"),
          websiteAddress = Some("/test/url")
        )),
        contactPreference = Some(Letter)
      )

      Json.toJson(modelToTransform)(BusinessContact.apiFormat) mustBe expectedJson
    }

    "transform a minimal BusinessContact into a minimal set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          |}
        """.stripMargin
      )

      val modelToTransform = BusinessContact()

      Json.toJson(modelToTransform)(BusinessContact.apiFormat) mustBe expectedJson
    }
  }
}
