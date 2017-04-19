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
import models.api.{VatComplianceFinancial, VatSicAndCompliance}
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class AdditionalNonSecuritiesWorkSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val additionalNonSecuritiesWork = AdditionalNonSecuritiesWork(false)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, additionalNonSecuritiesWork = Some(true)))
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, additionalNonSecuritiesWork = Some(false)))
    )

    "update VatSicAndCompliance with new AdditionalNonSecuritiesWork" in {
      ViewModelTransformer[AdditionalNonSecuritiesWork, VatSicAndCompliance]
        .toApi(additionalNonSecuritiesWork, vatSicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[AdditionalNonSecuritiesWork].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = None)))
      ApiModelTransformer[AdditionalNonSecuritiesWork].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Additional Non Securities Work yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        additionalNonSecuritiesWork = Some(true))))))
      ApiModelTransformer[AdditionalNonSecuritiesWork].toViewModel(vs) shouldBe Some(AdditionalNonSecuritiesWork(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Additional Non Securities Work no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        additionalNonSecuritiesWork = Some(false))))))
      ApiModelTransformer[AdditionalNonSecuritiesWork].toViewModel(vs) shouldBe Some(AdditionalNonSecuritiesWork(false))
    }

  }
}

