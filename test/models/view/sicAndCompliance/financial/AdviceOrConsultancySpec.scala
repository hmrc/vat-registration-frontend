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
import models.api.VatComplianceFinancial
import models.{ApiModelTransformer, S4LVatSicAndCompliance}
import uk.gov.hmrc.play.test.UnitSpec

class AdviceOrConsultancySpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[AdviceOrConsultancy].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[AdviceOrConsultancy].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Advice or Consultancy yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(true, true)))))
      ApiModelTransformer[AdviceOrConsultancy].toViewModel(vs) shouldBe Some(AdviceOrConsultancy(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Advice or Consultancy no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(false, false)))))
      ApiModelTransformer[AdviceOrConsultancy].toViewModel(vs) shouldBe Some(AdviceOrConsultancy(false))
    }

  }

  val testView = AdviceOrConsultancy(true)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(adviceOrConsultancy = Some(testView))

    "extract adviceOrConsultancy from s4LVatSicAndCompliance" in {
      AdviceOrConsultancy.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with adviceOrConsultancy" in {
      AdviceOrConsultancy.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).adviceOrConsultancy shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with adviceOrConsultancy" in {
      AdviceOrConsultancy.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).adviceOrConsultancy shouldBe Some(testView)
    }

  }
}

