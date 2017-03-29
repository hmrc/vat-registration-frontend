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

package models.view

import fixtures.VatRegistrationFixture
import models.ApiModelTransformer
import models.api.VatComplianceCultural
import models.view.sicAndCompliance.CulturalComplianceQ1
import uk.gov.hmrc.play.test.UnitSpec

class CulturalComplianceQ1Spec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[CulturalComplianceQ1].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without CulturalCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(culturalComplianceSection = None)))
      ApiModelTransformer[CulturalComplianceQ1].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with CulturalCompliance section to view model - for profit" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(culturalComplianceSection = Some(VatComplianceCultural(notForProfit = false)))))
      ApiModelTransformer[CulturalComplianceQ1].toViewModel(vs) shouldBe Some(CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_NO))
    }

    "convert VatScheme with CulturalCompliance section to view model - not for profit" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(culturalComplianceSection = Some(VatComplianceCultural(notForProfit = true)))))
      ApiModelTransformer[CulturalComplianceQ1].toViewModel(vs) shouldBe Some(CulturalComplianceQ1(CulturalComplianceQ1.NOT_PROFIT_YES))
    }

  }
}

