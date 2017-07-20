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

class ChargeFeesSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[ChargeFees].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = None)))
      ApiModelTransformer[ChargeFees].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Charge Fees yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        chargeFees = Some(true))))))
      ApiModelTransformer[ChargeFees].toViewModel(vs) shouldBe Some(ChargeFees(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Charge Fees no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(VatComplianceFinancial(
                                                                                                        true,
                                                                                                        true,
                                                                                                        chargeFees = Some(false))))))
      ApiModelTransformer[ChargeFees].toViewModel(vs) shouldBe Some(ChargeFees(false))
    }

  }

  val testView = ChargeFees(true)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(chargeFees = Some(testView))

    "extract chargeFees from s4LVatSicAndCompliance" in {
      ChargeFees.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with chargeFees" in {
      ChargeFees.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).chargeFees shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with chargeFees" in {
      ChargeFees.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).chargeFees shouldBe Some(testView)
    }

  }
}

