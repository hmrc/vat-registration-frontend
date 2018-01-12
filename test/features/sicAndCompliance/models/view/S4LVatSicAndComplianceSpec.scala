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

package features.sicAndCompliance.models.view

import models.S4LVatSicAndCompliance
import models.api.SicCode
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.labour.CompanyProvideWorkers._
import models.view.sicAndCompliance.labour.TemporaryContracts._
import models.view.sicAndCompliance.labour.SkilledWorkers._
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class S4LVatSicAndComplianceSpec extends UnitSpec {
  "fromApiReads" should {
    "return a valid view model for a none labour SIC Code" in {
      val json = Json.parse(
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

      val sicCode = SicCode(id = "123", description = "none labour", displayDetails = "none labour")
      val expected = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription("Test Desc")),
        mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.id, mainBusinessActivity = Some(sicCode)))
      )

      S4LVatSicAndCompliance.fromApiReads(json) shouldBe expected
    }

    "return a valid view model for a labour SIC Code with workers" in {
      val json = Json.parse(
        s"""
           |{
           |  "businessDescription": "Test Desc",
           |  "mainBusinessActivity": {
           |    "id": "123",
           |    "description": "labour",
           |    "displayDetails": "labour"
           |  },
           |  "labourCompliance": {
           |    "numberOfWorkers": 5,
           |    "temporaryContracts": true,
           |    "skilledWorkers": true
           |  }
           |}
         """.stripMargin)

      val sicCode = SicCode(id = "123", description = "labour", displayDetails = "labour")
      val expected = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription("Test Desc")),
        mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.id, mainBusinessActivity = Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_YES)),
        workers = Some(Workers(5)),
        temporaryContracts = Some(TemporaryContracts(TEMP_CONTRACTS_YES)),
        skilledWorkers = Some(SkilledWorkers(SKILLED_WORKERS_YES))
      )

      S4LVatSicAndCompliance.fromApiReads(json) shouldBe expected
    }

    "return a valid view model for a labour SIC Code without workers" in {
      val json = Json.parse(
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

      val sicCode = SicCode(id = "123", description = "labour", displayDetails = "labour")
      val expected = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription("Test Desc")),
        mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.id, mainBusinessActivity = Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(PROVIDE_WORKERS_NO)),
        workers = None,
        temporaryContracts = None,
        skilledWorkers = None
      )

      S4LVatSicAndCompliance.fromApiReads(json) shouldBe expected
    }
  }
}
