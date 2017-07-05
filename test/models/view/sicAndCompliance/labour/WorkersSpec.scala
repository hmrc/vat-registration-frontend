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
import models.api.{VatComplianceLabour, VatSicAndCompliance}
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class WorkersSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val workers = Workers(5)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      labourCompliance = Some(VatComplianceLabour(labour = true, workers = Some(6)))
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      labourCompliance = Some(VatComplianceLabour(labour = true, workers = Some(5)))
    )

    "update VatSicAndCompliance with new TemporaryContracts" in {
      ViewModelTransformer[Workers, VatSicAndCompliance]
        .toApi(workers, vatSicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[Workers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without LabourCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = None)))
      ApiModelTransformer[Workers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with LabourCompliance section to view model" in {
      val vs = vatScheme(sicAndCompliance =
        Some(vatSicAndCompliance(labourComplianceSection =
          Some(VatComplianceLabour(true, Some(8), Some(true), Some(true))))))

      ApiModelTransformer[Workers].toViewModel(vs) shouldBe Some(Workers(8))
    }

  }

  val testView = Workers(1)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(workers = Some(testView))

    "extract workers from s4LVatSicAndCompliance" in {
      Workers.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with workers" in {
      Workers.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).workers shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with workers" in {
      Workers.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).workers shouldBe Some(testView)
    }

  }
}

