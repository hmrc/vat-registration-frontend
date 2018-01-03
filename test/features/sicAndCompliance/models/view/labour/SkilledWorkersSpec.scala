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

package models.view.sicAndCompliance.labour

import fixtures.VatRegistrationFixture
import models.api.VatComplianceLabour
import models.{ApiModelTransformer, S4LVatSicAndCompliance}
import uk.gov.hmrc.play.test.UnitSpec

class SkilledWorkersSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[SkilledWorkers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without LabourCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[SkilledWorkers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with LabourCompliance section to view model -  Company Does not Provide Skilled Workers " in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            labourComplianceSection = Some(VatComplianceLabour(false, Some(8), Some(false), Some(false))))))
      ApiModelTransformer[SkilledWorkers].toViewModel(vs) shouldBe Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_NO))
    }

    "convert VatScheme with LabourCompliance section to view model - Company Does Provide Skilled Workers" in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            labourComplianceSection = Some(VatComplianceLabour(true, Some(8), Some(true), Some(true))))))
      ApiModelTransformer[SkilledWorkers].toViewModel(vs) shouldBe Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
    }

  }

  val testView = SkilledWorkers("yes")

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(skilledWorkers = Some(testView))

    "extract skilledWorkers from s4LVatSicAndCompliance" in {
      SkilledWorkers.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with skilledWorkers" in {
      SkilledWorkers.viewModelFormat.update(testView,
        Option.empty[S4LVatSicAndCompliance]).skilledWorkers shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with skilledWorkers" in {
      SkilledWorkers.viewModelFormat.update(testView,
        Some(s4LVatSicAndCompliance)).skilledWorkers shouldBe Some(testView)
    }

  }
}
