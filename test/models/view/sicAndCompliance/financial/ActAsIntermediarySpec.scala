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

package models.view.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import models.api.{SicCode, VatComplianceFinancial, VatSicAndCompliance}
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class ActAsIntermediarySpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val actAsIntermediary = ActAsIntermediary(false)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessDescription = businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(adviceOrConsultancyOnly = true, actAsIntermediary = true)),
      mainBusinessActivity = SicCode("","","")
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessDescription = businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(adviceOrConsultancyOnly = true, actAsIntermediary = false)),
      mainBusinessActivity = SicCode("","","")
    )

    "update VatSicAndCompliance with new ActAsIntermediary" in {
      ViewModelTransformer[ActAsIntermediary, VatSicAndCompliance]
        .toApi(actAsIntermediary, vatSicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[ActAsIntermediary].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[ActAsIntermediary].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Act as Intermediary yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(true, true)))))
      ApiModelTransformer[ActAsIntermediary].toViewModel(vs) shouldBe Some(ActAsIntermediary(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Act as Intermediary no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(false, false)))))
      ApiModelTransformer[ActAsIntermediary].toViewModel(vs) shouldBe Some(ActAsIntermediary(false))
    }

  }

  val testView = ActAsIntermediary(true)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(actAsIntermediary = Some(testView))

    "extract actAsIntermediary from s4LVatSicAndCompliance" in {
      ActAsIntermediary.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with ActAsIntermediary" in {
      ActAsIntermediary.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).actAsIntermediary shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with ActAsIntermediary" in {
      ActAsIntermediary.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).actAsIntermediary shouldBe Some(testView)
    }

  }
}

