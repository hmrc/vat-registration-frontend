/*
 * Copyright 2018 HM Revenue & Customs
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

package features.sicAndCompliance.models

import features.sicAndCompliance.models.CompanyProvideWorkers._
import features.sicAndCompliance.models.SkilledWorkers._
import features.sicAndCompliance.models.TemporaryContracts._
import models.api.SicCode
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SicAndComplianceSpec extends UnitSpec {
  val sicCodeNoneLabour = SicCode(id = "123", description = "none labour", displayDetails = "none labour")
  val sicCodeLabour = SicCode(id = "123", description = "labour", displayDetails = "labour")

  val jsonNoneLabour = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "id": "123",
       |    "description": "none labour",
       |    "displayDetails": "none labour"
       |  }
       |}
         """.stripMargin)
  val noneLabour = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeNoneLabour.id, mainBusinessActivity = Some(sicCodeNoneLabour)))
  )

  val jsonLabourWithoutWorkers = Json.parse(
    s"""
       |{
       |  "businessDescription": "Test Desc",
       |  "mainBusinessActivity": {
       |    "id": "123",
       |    "description": "labour",
       |    "displayDetails": "labour"
       |  },
       |  "labourCompliance": {
       |    "numberOfWorkers": 0
       |  }
       |}
         """.stripMargin)
  val labourWithoutWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.id, mainBusinessActivity = Some(sicCodeLabour))),
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
       |    "id": "123",
       |    "description": "labour",
       |    "displayDetails": "labour"
       |  },
       |  "labourCompliance": {
       |    "numberOfWorkers": 7
       |  }
       |}
         """.stripMargin)
  val labourWith7Workers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.id, mainBusinessActivity = Some(sicCodeLabour))),
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
       |    "id": "123",
       |    "description": "labour",
       |    "displayDetails": "labour"
       |  },
       |  "labourCompliance": {
       |    "numberOfWorkers": 8,
       |    "temporaryContracts": true,
       |    "skilledWorkers": true
       |  }
       |}
         """.stripMargin)
  val labourWith8PlusWorkers = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.id, mainBusinessActivity = Some(sicCodeLabour))),
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
       |    "id": "123",
       |    "description": "labour",
       |    "displayDetails": "labour"
       |  },
       |  "labourCompliance": {
       |    "numberOfWorkers": 8,
       |    "temporaryContracts": false
       |  }
       |}
         """.stripMargin)
  val labourWithoutTemporaryContracts = SicAndCompliance(
    description = Some(BusinessActivityDescription("Test Desc")),
    mainBusinessActivity = Some(MainBusinessActivityView(id = sicCodeLabour.id, mainBusinessActivity = Some(sicCodeLabour))),
    companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
    workers = Some(Workers(8)),
    temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_NO)),
    skilledWorkers = None
  )

  "fromApi" should {
    "return a valid view model for a none labour SIC Code" in {
      SicAndCompliance.fromApi(jsonNoneLabour) shouldBe noneLabour
    }

    "return a valid view model for a labour SIC Code without workers" in {
      SicAndCompliance.fromApi(jsonLabourWithoutWorkers) shouldBe labourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers (less than 8)" in {
      SicAndCompliance.fromApi(jsonLabourWith7Workers) shouldBe labourWith7Workers
    }

    "return a valid view model for a labour SIC Code with workers (more than 8)" in {
      SicAndCompliance.fromApi(jsonLabourWith8PlusWorkers) shouldBe labourWith8PlusWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      SicAndCompliance.fromApi(jsonLabourWithoutTemporaryContracts) shouldBe labourWithoutTemporaryContracts
    }
  }

  "toApiWrites" should {
    "return a valid api model json for a none labour SIC Code" in {
      Json.toJson(noneLabour)(SicAndCompliance.toApiWrites) shouldBe jsonNoneLabour
    }

    "return a valid api model json for a labour SIC Code without workers" in {
      Json.toJson(labourWithoutWorkers)(SicAndCompliance.toApiWrites) shouldBe jsonLabourWithoutWorkers
    }

    "return a valid view model for a labour SIC Code with workers (less than 8)" in {
      Json.toJson(labourWith7Workers)(SicAndCompliance.toApiWrites) shouldBe jsonLabourWith7Workers
    }

    "return a valid view model for a labour SIC Code with workers (more than 8)" in {
      Json.toJson(labourWith8PlusWorkers)(SicAndCompliance.toApiWrites) shouldBe jsonLabourWith8PlusWorkers
    }

    "return a valid view model for a labour SIC Code without temporary contracts" in {
      Json.toJson(labourWithoutTemporaryContracts)(SicAndCompliance.toApiWrites) shouldBe jsonLabourWithoutTemporaryContracts
    }
  }
}
