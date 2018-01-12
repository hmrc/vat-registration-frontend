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
import models.S4LVatSicAndCompliance
import models.view.SummaryRow
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}

class SummaryLabourComplianceSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {


  val defaultSicAndCompliance = S4LVatSicAndCompliance(
    description = Some(BusinessActivityDescription("TEST")),
    mainBusinessActivity = Some(MainBusinessActivityView("TEST",Some(sicCode))),
    companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
    workers = Some(Workers(12)),
    temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
    skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
  )

//  val testVatSicAndCompliance = Some(VatSicAndCompliance(businessDescription = "TEST", mainBusinessActivity = sicCode))

  "The section builder composing a labour details section" should {

    "providingWorkersRow render" should {

      " 'Yes' selected providingWorkersRow " in {
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(defaultSicAndCompliance))
        builder.providingWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesWorkers",
            "app.common.yes",
            Some(controllers.sicAndCompliance.labour.routes.CompanyProvideWorkersController.show())
          )
      }


      " 'No' selected for providingWorkersRow" in {
   val complianceUpdatedForTest = defaultSicAndCompliance.copy(companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)))
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(complianceUpdatedForTest))
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
        val builder = SummaryLabourComplianceSectionBuilder(Some(defaultSicAndCompliance))
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

        val updatedModelFortest = defaultSicAndCompliance.copy(temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)))
        val builder = SummaryLabourComplianceSectionBuilder(Some(updatedModelFortest))
        builder.temporaryContractsRow mustBe
          SummaryRow(
            "labourCompliance.workersOnTemporaryContracts",
            "app.common.no",
            Some(controllers.sicAndCompliance.labour.routes.TemporaryContractsController.show())
          )
      }


      " 'YES' selected for temporaryContractsRow" in {
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(defaultSicAndCompliance))
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
        val updatedModelFortest = defaultSicAndCompliance.copy(skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO)))
        val builder = SummaryLabourComplianceSectionBuilder(Some(updatedModelFortest))
        builder.skilledWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesSkilledWorkers",
            "app.common.no",
            Some(controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show())
          )
      }


      " 'YES' selected for skilledWorkersRow" in {
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(defaultSicAndCompliance))
        builder.skilledWorkersRow mustBe
          SummaryRow(
            "labourCompliance.providesSkilledWorkers",
            "app.common.yes",
            Some(controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show())
          )
      }
    }


    "section generate" should {
      "a valid summary section" in {
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(defaultSicAndCompliance))
        builder.section.id mustBe "labourCompliance"
        builder.section.rows.length mustEqual 4
      }
      "no valid summary section where user has not selected labour Compliance" in {
        val builder = SummaryLabourComplianceSectionBuilder(vatSicAndCompliance = Some(defaultSicAndCompliance.copy(
          companyProvideWorkers = None,
          workers  = None,
          temporaryContracts = None,
          skilledWorkers = None
        )))
        builder.section.rows.length mustEqual 4
        builder.section.display mustEqual false
      }
    }
  }
}
