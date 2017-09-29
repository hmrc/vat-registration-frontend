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

package models.view.sicAndCompliance

import fixtures.VatRegistrationFixture
import models.api.VatSicAndCompliance
import models.{ApiModelTransformer, S4LVatSicAndCompliance}
import uk.gov.hmrc.play.test.UnitSpec

class BusinessActivityDescriptionSpec extends UnitSpec with VatRegistrationFixture {
  val description_1 = "testing-1"
  val description_2 = "testing-2"
  val businessActivityDescription_1 = BusinessActivityDescription(description_1)
  val businessActivityDescription_2 = BusinessActivityDescription(description_2)

  val sicAndCompliance = VatSicAndCompliance(description_1, None,
    mainBusinessActivity = sicCode)
  val differentSicAndCompliance = VatSicAndCompliance(description_2, None,
    mainBusinessActivity = sicCode)

  "apply" should {

    "Extract a BusinessActivityDescription view model from a VatScheme" in {
      ApiModelTransformer[BusinessActivityDescription].toViewModel(
        vatScheme = vatScheme(
          sicAndCompliance = Some(vatSicAndCompliance(activityDescription = description_1, mainBusinessActivitySection = sicCode))
        )
      ) shouldBe Some(businessActivityDescription_1)
    }

    "Extract an empty BusinessActivityDescription view model from a VatScheme without sicAndCompliance" in {
      ApiModelTransformer[BusinessActivityDescription].toViewModel(vatScheme()) shouldBe None
    }
  }

  val testView = BusinessActivityDescription("activity")

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(description = Some(testView))

    "extract description from s4LVatSicAndCompliance" in {
      BusinessActivityDescription.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with description" in {
      BusinessActivityDescription.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).description shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with description" in {
      BusinessActivityDescription.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).description shouldBe Some(testView)
    }

  }
}