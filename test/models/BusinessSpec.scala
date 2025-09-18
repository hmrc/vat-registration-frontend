/*
 * Copyright 2024 HM Revenue & Customs
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

import models.api.{Address, SicCode}
import play.api.libs.json.{JsObject, JsValue, Json}
import testHelpers.VatRegSpec

class BusinessSpec extends VatRegSpec {

  val sicCodeNoneLabour: SicCode = SicCode(code = "123", description = "none labour", descriptionCy = "none labour")
  val sicCodeLabour: SicCode = SicCode(code = "123", description = "labour", descriptionCy = "labour")

  val jsonNoneLabour: JsObject = Json.parse(
    s"""
       |{
       |"businessDescription": "Test Desc",
       |"mainBusinessActivity": {
       |   "code": "123",
       |   "desc": "none labour",
       |   "descCy": "none labour"
       |},
       |"businessActivities": [
       |{
       |   "code": "99889",
       |   "desc": "otherBusiness",
       |   "descCy": "otherBusiness"
       |}
       |]
       |}
    """.stripMargin).as[JsObject]
  val noneLabour: Business = Business(
    businessDescription = Some("Test Desc"),
    mainBusinessActivity = Some(sicCodeNoneLabour),
    businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness")))
  )

  val jsonLabourWithoutWorkers: JsValue = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "descCy": "labour"
       |  },
       |  "businessActivities": [
       |  {
       |     "code": "99889",
       |     "desc": "otherBusiness",
       |     "descCy": "otherBusiness1"
       |  }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": false
       |  }
       |}
         """.stripMargin)
  val labourWithoutWorkers: Business = Business(
    businessDescription = Some("Test Desc"),
    mainBusinessActivity = Some(sicCodeLabour),
    businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1"))),
    labourCompliance = Some(LabourCompliance(
      supplyWorkers = Some(false),
      numOfWorkersSupplied = None,
      intermediaryArrangement = None
    ))
  )

  val jsonLabourWithWorkers: JsValue = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "descCy": "labour"
       |  },
       |  "businessActivities": [
       |    {
       |       "code": "99889",
       |       "desc": "otherBusiness",
       |       "descCy": "otherBusiness1"
       |    }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": true,
       |    "numOfWorkersSupplied": 7
       |  }
       |}
         """.stripMargin)

  val labourWithWorkers: Business = Business(
    businessDescription = Some("Test Desc"),
    mainBusinessActivity = Some(sicCodeLabour),
    businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1"))),
    labourCompliance = Some(LabourCompliance(
      supplyWorkers = Some(true),
      numOfWorkersSupplied = Some(7),
      intermediaryArrangement = None
    ))
  )

  val jsonLabourWithoutTemporaryContracts: JsValue = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "descCy": "labour"
       |  },
       |  "businessActivities": [
       |  {
       |     "code": "99889",
       |     "desc": "otherBusiness",
       |     "descCy": "otherBusiness1"
       |  }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": true,
       |    "numOfWorkersSupplied": 8,
       |    "intermediaryArrangement": false
       |  }
       |}
         """.stripMargin)

  val labourWithoutTemporaryContracts: Business = Business(
    businessDescription = Some("Test Desc"),
    mainBusinessActivity = Some(sicCodeLabour),
    businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1"))),
    labourCompliance = Some(LabourCompliance(
      supplyWorkers = Some(true),
      numOfWorkersSupplied = Some(8),
      intermediaryArrangement = Some(false)
    ))
  )

  "when parsing json from the api" should {
    "parse the full json into a BusinessModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          | "email" : "test@test.com",
          | "telephoneNumber" : "0123456",
          | "website" : "/test/url",
          | "ppobAddress" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "line3" : "testLine3",
          |   "line4" : "testLine4",
          |   "postcode" : "TE57 7ET",
          |   "addressValidated" : true
          | },
          | "contactPreference": "Email"
          |}
        """.stripMargin
      )

      val expectedModel = Business(
        ppobAddress = Some(Address(
          line1    = "testLine1",
          line2    = Some("testLine2"),
          line3    = Some("testLine3"),
          line4    = Some("testLine4"),
          postcode = Some("TE57 7ET"),
          addressValidated = true
        )),
        email           = Some("test@test.com"),
        telephoneNumber = Some("0123456"),
        website  = Some("/test/url"),
        contactPreference = Some(Email)
      )

      jsonToParse.as[Business] mustBe expectedModel
    }

    "parse the minimal json into a BusinessModel" in {
      val jsonToParse = Json.parse(
        """
          |{
          |}
        """.stripMargin
      )

      val expectedModel = Business()

      jsonToParse.as[Business] mustBe expectedModel
    }
  }

  "when converting the model to json for the API" should {
    "transform a full Business into a full set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "email" : "test@test.com",
          | "telephoneNumber" : "0123456",
          | "website" : "/test/url",
          | "ppobAddress" : {
          |   "line1" : "testLine1",
          |   "line2" : "testLine2",
          |   "line3" : "testLine3",
          |   "line4" : "testLine4",
          |   "postcode" : "TE57 7ET",
          |   "addressValidated" : true
          | },
          | "contactPreference": "Letter",
          | "businessDescription": "Test Desc",
          |  "mainBusinessActivity": {
          |    "code": "123",
          |    "desc": "labour",
          |    "descCy": "labour"
          |  },
          |  "businessActivities": [
          |  {
          |     "code": "99889",
          |     "desc": "otherBusiness",
          |     "descCy": "otherBusiness1"
          |  }
          |  ]
          |}
        """.stripMargin
      )

      val modelToTransform = Business(
        ppobAddress = Some(Address(
          line1    = "testLine1",
          line2    = Some("testLine2"),
          line3    = Some("testLine3"),
          line4    = Some("testLine4"),
          postcode = Some("TE57 7ET"),
          addressValidated = true
        )),
        email           = Some("test@test.com"),
        telephoneNumber = Some("0123456"),
        website  = Some("/test/url"),
        contactPreference = Some(Letter),
        businessDescription = Some("Test Desc"),
        mainBusinessActivity = Some(sicCodeLabour),
        businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1")))
      )

      Json.toJson(modelToTransform) mustBe expectedJson
    }

    "transform a Business with no website into json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "email" : "test@test.com",
          | "telephoneNumber" : "0123456",
          | "contactPreference": "Letter",
          | "businessDescription": "Test Desc",
          |  "mainBusinessActivity": {
          |    "code": "123",
          |    "desc": "labour",
          |    "descCy": "labour"
          |  },
          |  "businessActivities": [
          |  {
          |     "code": "99889",
          |     "desc": "otherBusiness",
          |     "descCy": "otherBusiness1"
          |  }
          |  ]
          |}
        """.stripMargin
      )

      val modelToTransform = Business(
        email           = Some("test@test.com"),
        telephoneNumber = Some("0123456"),
        website         = None,
        contactPreference = Some(Letter),
        businessDescription = Some("Test Desc"),
        mainBusinessActivity = Some(sicCodeLabour),
        businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1")))
      )

      Json.toJson(modelToTransform) mustBe expectedJson
    }

    "transform a minimal Business into a minimal set of Json" in {
      val expectedJson = Json.parse(
        """
          |{
          | "businessDescription": "Test Desc",
          |  "mainBusinessActivity": {
          |    "code": "123",
          |    "desc": "labour",
          |    "descCy": "labour"
          |  },
          |  "businessActivities": [
          |  {
          |     "code": "99889",
          |     "desc": "otherBusiness",
          |     "descCy": "otherBusiness1"
          |  }
          |  ]
          |}
        """.stripMargin
      )

      val modelToTransform = Business(
        businessDescription = Some("Test Desc"),
        mainBusinessActivity = Some(sicCodeLabour),
        businessActivities = Some(List(SicCode(code="99889", description = "otherBusiness", descriptionCy = "otherBusiness1")))
      )

      Json.toJson(modelToTransform) mustBe expectedJson
    }
  }

  "api read" should {


    "return a valid view model for a none labour SIC Code" in {
      jsonNoneLabour.as[Business] mustBe noneLabour
    }

    "return a valid view model for a labour SIC Code without workers" in {
      jsonLabourWithoutWorkers.as[Business] mustBe labourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers" in {
      jsonLabourWithWorkers.as[Business] mustBe labourWithWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      jsonLabourWithoutTemporaryContracts.as[Business] mustBe labourWithoutTemporaryContracts
    }
  }

  "toApiWrites" should {
    "return a valid api model json for a none labour SIC Code" in {
      Json.toJson(noneLabour) mustBe jsonNoneLabour
    }

    "return a valid api model json for a labour SIC Code without workers" in {
      Json.toJson(labourWithoutWorkers) mustBe jsonLabourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers" in {
      Json.toJson(labourWithWorkers) mustBe jsonLabourWithWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      Json.toJson(labourWithoutTemporaryContracts) mustBe jsonLabourWithoutTemporaryContracts
    }
  }
}
