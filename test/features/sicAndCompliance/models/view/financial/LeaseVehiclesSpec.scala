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

package models.view.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import models.api.VatComplianceFinancial
import models.{ApiModelTransformer, S4LVatSicAndCompliance}
import uk.gov.hmrc.play.test.UnitSpec

class LeaseVehiclesSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[LeaseVehicles].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without FinancialCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(
        financialComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[LeaseVehicles].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with FinancialCompliance section to view model - Lease Vehicles or Equipment yes" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(
        mainBusinessActivitySection = sicCode, financialComplianceSection = Some(
          VatComplianceFinancial(
            adviceOrConsultancyOnly = true,
            actAsIntermediary = true,
            vehicleOrEquipmentLeasing = Some(true))))))
      ApiModelTransformer[LeaseVehicles].toViewModel(vs) shouldBe Some(LeaseVehicles(true))
    }

    "convert VatScheme with FinancialCompliance section to view model - Lease Vehicles or Equipment no" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCode, financialComplianceSection = Some(
        VatComplianceFinancial(
          adviceOrConsultancyOnly = true,
          actAsIntermediary = true,
          vehicleOrEquipmentLeasing = Some(false))))))
      ApiModelTransformer[LeaseVehicles].toViewModel(vs) shouldBe Some(LeaseVehicles(false))
    }

  }

  val testView = LeaseVehicles(true)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(leaseVehicles = Some(testView))

    "extract leaseVehicles from s4LVatSicAndCompliance" in {
      LeaseVehicles.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with leaseVehicles" in {
      LeaseVehicles.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).leaseVehicles shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with leaseVehicles" in {
      LeaseVehicles.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).leaseVehicles shouldBe Some(testView)
    }

  }
}
