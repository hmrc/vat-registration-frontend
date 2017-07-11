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
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class InvestmentFundManagementSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val investmentFundManagementServices = InvestmentFundManagement(false)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, investmentFundManagementServices = Some(true))),
      mainBusinessActivity = sicCode
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessActivityDescription,
      financialCompliance = Some(VatComplianceFinancial(true, true, investmentFundManagementServices = Some(false))),
      mainBusinessActivity = sicCode
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
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(financialComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Investment Fund Management yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        investmentFundManagementServices = Some(true))))))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe Some(InvestmentFundManagement(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Investment Fund Management no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        investmentFundManagementServices = Some(false))))))
      ApiModelTransformer[InvestmentFundManagement].toViewModel(vs) shouldBe Some(InvestmentFundManagement(false))
    }

  }

  val testView = InvestmentFundManagement(true)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(investmentFundManagement = Some(testView))

    "extract investmentFundManagement from s4LVatSicAndCompliance" in {
      InvestmentFundManagement.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with investmentFundManagement" in {
      InvestmentFundManagement.viewModelFormat.update(testView,
        Option.empty[S4LVatSicAndCompliance]).investmentFundManagement shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with investmentFundManagement" in {
      InvestmentFundManagement.viewModelFormat.update(testView,
        Some(s4LVatSicAndCompliance)).investmentFundManagement shouldBe Some(testView)
    }

  }
}

