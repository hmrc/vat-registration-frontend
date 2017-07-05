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
import models.api.{VatComplianceCultural, VatSicAndCompliance}
import models.view.SummaryRow

class SummaryCulturalComplianceSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a cultural details section" should {

    "with notForProfitRow render" should {

      " 'YES' selected for notForProfitRow" in {
        val compliance = VatSicAndCompliance("Business Described", culturalCompliance = Some(VatComplianceCultural(notForProfit = true)), mainBusinessActivity = sicCode)
        val builder = SummaryCulturalComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.notForProfitRow mustBe
          SummaryRow(
            "culturalCompliance.notForProfitOrganisation",
            "app.common.yes",
            Some(controllers.sicAndCompliance.cultural.routes.NotForProfitController.show())
          )
      }

      " 'NO' selected for notForProfitRow" in {
        val compliance = VatSicAndCompliance("Business Described", culturalCompliance = Some(VatComplianceCultural(notForProfit = false)), mainBusinessActivity = sicCode)
        val builder = SummaryCulturalComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.notForProfitRow mustBe
          SummaryRow(
            "culturalCompliance.notForProfitOrganisation",
            "app.common.no",
            Some(controllers.sicAndCompliance.cultural.routes.NotForProfitController.show())
          )
      }

    }

    "with section generate" should {
      val compliance = VatSicAndCompliance("Business Described", culturalCompliance = Some(VatComplianceCultural(notForProfit = true)), mainBusinessActivity = sicCode)
      val builder = SummaryCulturalComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))

      "a valid summary section" in {
        val builder = SummaryCulturalComplianceSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.section.id mustBe "culturalCompliance"
        builder.section.rows.length mustEqual 1
      }
    }

  }
}
