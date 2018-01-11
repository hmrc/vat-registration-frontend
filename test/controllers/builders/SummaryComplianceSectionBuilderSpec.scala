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

package controllers

import common.enums.VatRegStatus
import controllers.builders.SummaryComplianceSectionBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatScheme, VatSicAndCompliance}

class SummaryComplianceSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "Correct compliance section should be rendered" when {

    "labour questions have been answered by user" in {
      val vs = VatScheme("ID",status = VatRegStatus.draft, vatSicAndCompliance = Some(VatSicAndCompliance("TEST", labourCompliance = Some(validVatLabourCompliance), mainBusinessActivity = sicCode)))
      val summarySection = SummaryComplianceSectionBuilder(vatSicAndCompliance = vs.vatSicAndCompliance)
      summarySection.section.id mustBe "labourCompliance"
    }

    "No compliance questions have been answered by user" in {
      val vs = VatScheme("ID", status = VatRegStatus.draft, vatSicAndCompliance = None)
      val summarySection = SummaryComplianceSectionBuilder(vatSicAndCompliance = vs.vatSicAndCompliance)
      summarySection.section.id mustBe "none"
    }

  }

}
