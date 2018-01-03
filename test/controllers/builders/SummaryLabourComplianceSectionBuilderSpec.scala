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

package controllers.builders

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatComplianceLabour, VatSicAndCompliance}
import models.view.SummaryRow

class SummaryLabourComplianceSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val defaultCompliance = VatComplianceLabour(
    labour = true,
    workers = Some(12),
    temporaryContracts = Some(true),
    skilledWorkers = Some(true)
  )

  val testVatSicAndCompliance = Some(VatSicAndCompliance(businessDescription = "TEST", mainBusinessActivity = sicCode))

  "The section builder composing a labour details section" should {


    "providingWorkersRow render" should {

      " 'Yes' selected providingWorkersRow " in {
        val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance.copy(labour = true)), mainBusinessActivity = sicCode)
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.providingWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesWorkers",
            "app.common.yes",
            Some(controllers.sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())
          )
      }


      " 'No' selected for providingWorkersRow" in {
        val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance.copy(labour = false)), mainBusinessActivity = sicCode)
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.providingWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesWorkers",
            "app.common.no",
            Some(controllers.sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())
          )
      }
    }


    "numberOfWorkers render" should {

      "render a row" in {
        val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance), mainBusinessActivity = sicCode)
        val builder = SummaryLabourComplianceSectionBuilder(Some(compliance))
        builder.numberOfWorkersRow mustBe
          SummaryRow(
            "labourCompliance.numberOfWorkers",
            "12",
            Some(controllers.sicAndCompliance.labour.routes.WorkersController.show())
          )
      }

    }


    "temporaryContractsRow render" should {

      " 'No' selected temporaryContractsRow " in {
        val builder = SummaryLabourComplianceSectionBuilder()
        builder.temporaryContractsRow mustBe
          SummaryRow(
            "labourCompliance.workersOnTemporaryContracts",
            "app.common.no",
            Some(controllers.sicAndCompliance.labour.routes.TemporaryContractsController.show())
          )
      }


      " 'YES' selected for temporaryContractsRow" in {
        val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance), mainBusinessActivity = sicCode)
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.temporaryContractsRow mustBe
          SummaryRow(
            "labourCompliance.workersOnTemporaryContracts",
            "app.common.yes",
            Some(controllers.sicAndCompliance.labour.routes.TemporaryContractsController.show())
          )
      }
    }

    "skilledWorkersRow render" should {

      " 'No' selected skilledWorkersRow " in {
        val builder = SummaryLabourComplianceSectionBuilder()
        builder.skilledWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesSkilledWorkers",
            "app.common.no",
            Some(controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show())
          )
      }


      " 'YES' selected for skilledWorkersRow" in {
        val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance), mainBusinessActivity = sicCode)
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.skilledWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesSkilledWorkers",
            "app.common.yes",
            Some(controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show())
          )
      }
    }


    "section generate" should {
      val compliance = VatSicAndCompliance("Business Described", labourCompliance = Some(defaultCompliance), mainBusinessActivity = sicCode)
      val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))

      "a valid summary section" in {
        builder.section.id mustBe "labourCompliance"
        builder.section.rows.length mustEqual 4
      }
    }

  }
}
