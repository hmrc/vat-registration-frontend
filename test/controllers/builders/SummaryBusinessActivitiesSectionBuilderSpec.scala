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

package controllers.builders

import features.sicAndCompliance.models.OtherBusinessActivities
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.SummaryRow

class SummaryBusinessActivitiesSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a company details section" should {

    val bankAccount = VatBankAccount(accountNumber = "12345678", accountName = "Account Name", accountSortCode = testSortCode)

    "with companyBusinessDescriptionRow render" should {

      "a 'No' value should be returned with an empty description in sic and compliance" in {
        val builder = SummaryBusinessActivitiesSectionBuilder()
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "businessActivities.businessDescription",
            "app.common.no",
            Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showBusinessActivityDescription())
          )
      }

      "a business activity description in sic and compliance should be shown when one is entered by the user" in {
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance = Some(s4lVatSicAndComplianceWithLabour))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "businessActivities.businessDescription",
            testBusinessActivityDescription,
            Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showBusinessActivityDescription())
          )
      }

      "a main business activity in sic and compliance should be shown when one is entered by the user" in {
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance = Some(s4lVatSicAndComplianceWithLabour))
        builder.companyMainBusinessActivityRow mustBe
          SummaryRow(
            "businessActivities.mainBusinessActivity",
            sicCode.code + " - " + sicCode.description,
            Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showMainBusinessActivity())
          )
      }

      "sic codes entered in sic and compliance should be shown when they are entered by the user" in {
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance =
          Some(s4lVatSicAndComplianceWithLabour.copy(otherBusinessActivities = Some(OtherBusinessActivities(List(sicCode))))
        ))
        builder.confirmIndustryClassificationCodesRow mustBe
          SummaryRow(
            "businessActivities.otherBusinessActivities",
            List(sicCode.code + " - " + sicCode.description),
            Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.returnToICL())
          )
      }

      "multiple sic codes entered in sic and compliance should be shown when they are entered by the user" in {
        val sicOne = sicCode.copy(description = "test")
        val sicTwo = sicCode.copy(description = "test2")

        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance =
          Some(s4lVatSicAndComplianceWithLabour.copy(otherBusinessActivities = Some(OtherBusinessActivities(List(sicOne, sicTwo))))
        ))
        builder.confirmIndustryClassificationCodesRow mustBe
          SummaryRow(
            "businessActivities.otherBusinessActivities",
            List(sicOne.code + " - " + sicOne.description, sicTwo.code + " - " + sicTwo.description),
            Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.returnToICL())
          )
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance = Some(s4lVatSicAndComplianceWithLabour))
        builder.section.id mustBe "businessActivities"
        builder.section.rows.length mustEqual 3
      }
    }
  }
}
