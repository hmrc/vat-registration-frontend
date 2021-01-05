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

package models

import models.api.SicCode
import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec

class SicAndComplianceSpec extends VatRegSpec {
  val sicCodeNoneLabour = SicCode(code = "123", description = "none labour", displayDetails = "none labour")
  val sicCodeLabour = SicCode(code = "123", description = "labour", displayDetails = "labour")

  val jsonNoneLabour = Json.parse(
    s"""
       |{
       |"businessDescription": "Test Desc",
       |"mainBusinessActivity": {
       |   "code": "123",
       |   "desc": "none labour",
       |   "indexes": "none labour"
       |},
       |"businessActivities": [
       |{
       |   "code": "99889",
       |   "desc": "otherBusiness",
       |   "indexes": ""
       |}
       |]
       |}
    """.stripMargin).as[JsObject]
  val noneLabour = SicAndCompliance(
      description = Some(BusinessActivityDescription("Test Desc")),
      mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeNoneLabour.code, mainBusinessActivity = Some(sicCodeNoneLabour))),
      businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = ""))))
    )

  val jsonLabourWithoutWorkers = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "indexes": "labour"
       |  },
       |  "businessActivities": [
       |  {
       |     "code": "99889",
       |     "desc": "otherBusiness",
       |     "indexes": "otherBusiness1"
       |  }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": false
       |  }
       |}
         """.stripMargin)
  val labourWithoutWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    supplyWorkers = Some(SupplyWorkers(false)),
    workers = None,
    intermediarySupply = None
  )

  val jsonLabourWithWorkers = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "indexes": "labour"
       |  },
       |  "businessActivities": [
       |    {
       |       "code": "99889",
       |       "desc": "otherBusiness",
       |       "indexes": "otherBusiness1"
       |    }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": true,
       |    "numOfWorkersSupplied": 7
       |  }
       |}
         """.stripMargin)
  val labourWithWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    supplyWorkers = Some(SupplyWorkers(true)),
    workers = Some(Workers(7)),
    intermediarySupply = None
  )

  val jsonLabourWithoutTemporaryContracts = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "code": "123",
       |    "desc": "labour",
       |    "indexes": "labour"
       |  },
       |  "businessActivities": [
       |  {
       |     "code": "99889",
       |     "desc": "otherBusiness",
       |     "indexes": "otherBusiness1"
       |  }
       |  ],
       |  "labourCompliance": {
       |    "supplyWorkers": true,
       |    "numOfWorkersSupplied": 8,
       |    "intermediaryArrangement": false
       |  }
       |}
         """.stripMargin)

  val labourWithoutTemporaryContracts = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    supplyWorkers = Some(SupplyWorkers(true)),
    workers = Some(Workers(8)),
    intermediarySupply = Some(IntermediarySupply(false))
  )

  "fromApi" should {
    "return a valid view model for a none labour SIC Code" in {
      SicAndCompliance.fromApi(jsonNoneLabour) mustBe noneLabour
    }

    "return a valid view model for a labour SIC Code without workers" in {
      SicAndCompliance.fromApi(jsonLabourWithoutWorkers) mustBe labourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers" in {
      SicAndCompliance.fromApi(jsonLabourWithWorkers) mustBe labourWithWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      SicAndCompliance.fromApi(jsonLabourWithoutTemporaryContracts) mustBe labourWithoutTemporaryContracts
    }
  }

  "toApiWrites" should {
    "return a valid api model json for a none labour SIC Code" in {
      Json.toJson(noneLabour)(SicAndCompliance.toApiWrites) mustBe jsonNoneLabour
    }

    "return a valid api model json for a labour SIC Code without workers" in {
      Json.toJson(labourWithoutWorkers)(SicAndCompliance.toApiWrites) mustBe jsonLabourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers" in {
      Json.toJson(labourWithWorkers)(SicAndCompliance.toApiWrites) mustBe jsonLabourWithWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      Json.toJson(labourWithoutTemporaryContracts)(SicAndCompliance.toApiWrites) mustBe jsonLabourWithoutTemporaryContracts
    }

    "return an Exception" when {
      "the view model is missing the business description" in {
        an[IllegalStateException] shouldBe thrownBy(Json.toJson(noneLabour.copy(description = None))(SicAndCompliance.toApiWrites))
      }

      "the view model is missing the SIC Code" in {
        val data = MainBusinessActivityView(id = sicCodeNoneLabour.code, mainBusinessActivity = None)
        an[IllegalStateException] shouldBe thrownBy(Json.toJson(noneLabour.copy(mainBusinessActivity = Some(data)))(SicAndCompliance.toApiWrites))
      }
    }
  }
}
