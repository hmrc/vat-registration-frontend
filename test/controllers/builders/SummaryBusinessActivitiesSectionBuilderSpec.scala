/*
 * Copyright 2017 HM Revenue & Customs
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
import models.api._
import models.view.SummaryRow
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency

class SummaryBusinessActivitiesSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a company details section" should {

    val bankAccount = VatBankAccount(accountNumber = "12345678", accountName = "Account Name", accountSortCode = sortCode)

    "with companyBusinessDescriptionRow render" should {

      "a 'No' value should be returned with an empty description in sic and compliance" in {
        val builder = SummaryBusinessActivitiesSectionBuilder()
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "businessActivities.businessDescription",
            "app.common.no",
            Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          )
      }

      "a business activity description in sic and compliance should be shown when one is entered by the user" in {
        val compliance = VatSicAndCompliance("Business Described", None)
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "businessActivities.businessDescription",
            "Business Described",
            Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          )
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance = Some(validSicAndCompliance))
        builder.section.id mustBe "businessActivities"
        builder.section.rows.length mustEqual 1
      }
    }
  }
}