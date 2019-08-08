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

package features.businessContact.models

import models.api.ScrsAddress
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class BusinessContactSpec extends UnitSpec {

  "fromApi" should {
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
          | }
          |}
        """.stripMargin
      )

      val expectedModel = BusinessContact(
        ppobAddress = Some(ScrsAddress(
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
        ))
      )

      BusinessContact.fromApi(jsonToParse) shouldBe expectedModel
    }

    "parse the minimal json into a BusinessContactModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com"
          | },
          | "ppob" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "postcode" : "TE57 7ET"
          | }
          |}
        """.stripMargin
      )

      val expectedModel = BusinessContact(
        ppobAddress = Some(ScrsAddress(
          line1    = "testLine1",
          line2    = "testLine2",
          line3    = None,
          line4    = None,
          postcode = Some("TE57 7ET")
        )),
        companyContactDetails = Some(CompanyContactDetails(
          email          = "test@test.com",
          phoneNumber    = None,
          mobileNumber   = None,
          websiteAddress = None
        ))
      )

      BusinessContact.fromApi(jsonToParse) shouldBe expectedModel
    }
  }

  "toApi" should {
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
          | }
          |}
        """.stripMargin
      )

      val modelToTransform = BusinessContact(
        ppobAddress = Some(ScrsAddress(
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
        ))
      )

      BusinessContact.toApi(modelToTransform) shouldBe expectedJson
    }

    "transform a minimal BusinessContact into a minimal set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "digitalContact" : {
          |   "email" : "test@test.com"
          | },
          | "ppob" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "postcode" : "TE57 7ET"
          | }
          |}
        """.stripMargin
      )

      val modelToTransform = BusinessContact(
        ppobAddress = Some(ScrsAddress(
          line1    = "testLine1",
          line2    = "testLine2",
          line3    = None,
          line4    = None,
          postcode = Some("TE57 7ET")
        )),
        companyContactDetails = Some(CompanyContactDetails(
          email          = "test@test.com",
          phoneNumber    = None,
          mobileNumber   = None,
          websiteAddress = None
        ))
      )

      BusinessContact.toApi(modelToTransform) shouldBe expectedJson
    }
  }
}
