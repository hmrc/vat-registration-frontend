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

package models.view.sicAndCompliance.labour

import fixtures.VatRegistrationFixture
import models.api.{SicCode, VatComplianceLabour, VatSicAndCompliance}
import models.view.sicAndCompliance.labour.TemporaryContracts.TEMP_CONTRACTS_NO
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class TemporaryContractsSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[TemporaryContracts].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without LabourCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[TemporaryContracts].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with LabourCompliance section to view model - temporary contracts yes" in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            labourComplianceSection = Some(VatComplianceLabour(true, Some(8), Some(true), Some(true))))))
      ApiModelTransformer[TemporaryContracts].toViewModel(vs) shouldBe Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES))
    }

    "convert VatScheme with LabourCompliance section to view model - temporary contracts no" in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            labourComplianceSection = Some(VatComplianceLabour(true, Some(8), Some(false), Some(true))))))
      ApiModelTransformer[TemporaryContracts].toViewModel(vs) shouldBe Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO))
    }

  }

  val testView = TemporaryContracts("yes")

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(temporaryContracts = Some(testView))

    "extract temporaryContracts from s4LVatSicAndCompliance" in {
      TemporaryContracts.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with temporaryContracts" in {
      TemporaryContracts.viewModelFormat.update(testView,
        Option.empty[S4LVatSicAndCompliance]).temporaryContracts shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with temporaryContracts" in {
      TemporaryContracts.viewModelFormat.update(testView,
        Some(s4LVatSicAndCompliance)).temporaryContracts shouldBe Some(testView)
    }

  }
}

