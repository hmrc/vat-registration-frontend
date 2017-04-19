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

class InvestmentFundManagementSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val investmentFundManagementServices = InvestmentFundManagement(false)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, investmentFundManagementServices = Some(true)))
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, investmentFundManagementServices = Some(false)))
    )

    "update VatFinancials with new AccountingPeriod" in {
      ViewModelTransformer[InvestmentFundManagement, VatSicAndCompliance]
        .toApi(investmentFundManagementServices, vatSicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = None)))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Investment Fund Management yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        investmentFundManagementServices = Some(true))))))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe Some(InvestmentFundManagement(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Investment Fund Management no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        investmentFundManagementServices = Some(false))))))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe Some(InvestmentFundManagement(false))
    }

  }
}

