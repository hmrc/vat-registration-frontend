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

import models.CompanyProvideWorkers._
import models.SkilledWorkers._
import models.TemporaryContracts._
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
       |    "numberOfWorkers": 0
       |  }
       |}
         """.stripMargin)
  val labourWithoutWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_NO)),
    workers = None,
    temporaryContracts = None,
    skilledWorkers = None
  )

  val jsonLabourWith7Workers = Json.parse(
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
       |    "numberOfWorkers": 7
       |  }
       |}
         """.stripMargin)
  val labourWith7Workers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
    workers = Some(Workers(7)),
    temporaryContracts = None,
    skilledWorkers = None
  )

  val jsonLabourWith8PlusWorkers = Json.parse(
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
       |    "numberOfWorkers": 8,
       |    "temporaryContracts": true,
       |    "skilledWorkers": true
       |  }
       |}
         """.stripMargin)
  val labourWith8PlusWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
    workers = Some(Workers(8)),
    temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_YES)),
    skilledWorkers = Some(SkilledWorkers(SKILLED_WORKERS_YES))
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
       |    "numberOfWorkers": 8,
       |    "temporaryContracts": false
       |  }
       |}
         """.stripMargin)
  val labourWithoutTemporaryContracts = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
    workers = Some(Workers(8)),
    temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_NO)),
    skilledWorkers = None
  )

  val jsonLabourWithoutSkilledWorkers = Json.parse(
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
       |    "numberOfWorkers": 8,
       |    "temporaryContracts": true,
       |    "skilledWorkers": false
       |  }
       |}
         """.stripMargin)
  val labourWithoutSkilledWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.code, mainBusinessActivity = Some(sicCodeLabour))),
    businessActivities = Some(BusinessActivities(List(SicCode(code="99889", description = "otherBusiness", displayDetails = "otherBusiness1")))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
    workers = Some(Workers(8)),
    temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_YES)),
    skilledWorkers = Some(SkilledWorkers(SKILLED_WORKERS_NO))
  )

  "fromApi" should {
    "return a valid view model for a none labour SIC Code" in {
      SicAndCompliance.fromApi(jsonNoneLabour) mustBe noneLabour
    }

    "return a valid view model for a labour SIC Code without workers" in {
      SicAndCompliance.fromApi(jsonLabourWithoutWorkers) mustBe labourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers (less than 8)" in {
      SicAndCompliance.fromApi(jsonLabourWith7Workers) mustBe labourWith7Workers
    }

    "return a valid view model for a labour SIC Code with workers (more than 8)" in {
      SicAndCompliance.fromApi(jsonLabourWith8PlusWorkers) mustBe labourWith8PlusWorkers
    }

    "return a valid view model for a labour SIC Code without skilled workers" in {
      SicAndCompliance.fromApi(jsonLabourWithoutSkilledWorkers) mustBe labourWithoutSkilledWorkers
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

    "return a valid view model for a labour SIC Code with workers (less than 8)" in {
      Json.toJson(labourWith7Workers)(SicAndCompliance.toApiWrites) mustBe jsonLabourWith7Workers
    }

    "return a valid view model for a labour SIC Code with workers (more than 8)" in {
      Json.toJson(labourWith8PlusWorkers)(SicAndCompliance.toApiWrites) mustBe jsonLabourWith8PlusWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      Json.toJson(labourWithoutTemporaryContracts)(SicAndCompliance.toApiWrites) mustBe jsonLabourWithoutTemporaryContracts
    }

    "return an Exception" when {
      "the view model is missing the business description" in {
        an[IllegalStateException] shouldBe thrownBy(Json.toJson(noneLabour.copy(description = None))(SicAndCompliance.toApiWrites))
      }

      "the view model is missing the number of workers" in {
        an[IllegalStateException] shouldBe thrownBy(Json.toJson(labourWith7Workers.copy(workers = None))(SicAndCompliance.toApiWrites))
      }

      "the view model is missing the SIC Code" in {
        val data = MainBusinessActivityView(id = sicCodeNoneLabour.code, mainBusinessActivity = None)
        an[IllegalStateException] shouldBe thrownBy(Json.toJson(noneLabour.copy(mainBusinessActivity = Some(data)))(SicAndCompliance.toApiWrites))
      }
    }
  }
}
