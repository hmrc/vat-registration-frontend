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
import models.api.{SicCode, VatSicAndCompliance}
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class MainBusinessActivitySpec extends UnitSpec with VatRegistrationFixture {
  val description_1 = "testing-1"
  val description_2 = "testing-2"
  val sicCodeId_1 = SicCode("70221001", "sicCodeId_1", "sicCodeId_1")
  val sicCodeId_2 =  SicCode("70221002", "sicCodeId_2", "sicCodeId_2")
  val mainBusinessActivity_1 = MainBusinessActivityView(sicCodeId_1.id, Some(sicCodeId_1))
  val mainBusinessActivity_2 = MainBusinessActivityView(sicCodeId_2.id, Some(sicCodeId_2))

  val sicAndCompliance = VatSicAndCompliance(description_1, None, mainBusinessActivity = sicCodeId_1)
  val differentSicAndCompliance = VatSicAndCompliance(description_1, None,
    mainBusinessActivity = sicCodeId_2)

  "toApi" should {
    "update a SicAndCompliance with new MainBusinessActivityView" in {
      ViewModelTransformer[MainBusinessActivityView, VatSicAndCompliance]
        .toApi(mainBusinessActivity_2, sicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "Extract a MainBusinessActivityView view model from a VatScheme" in {
      ApiModelTransformer[MainBusinessActivityView].toViewModel(
        vatScheme = vatScheme(
          sicAndCompliance = Some(vatSicAndCompliance(mainBusinessActivitySection = sicCodeId_1 ))
        )
      ) shouldBe Some(mainBusinessActivity_1)
    }

    "Extract an empty MainBusinessActivityView view model from a VatScheme without sicAndCompliance" in {
      ApiModelTransformer[MainBusinessActivityView].toViewModel(vatScheme()) shouldBe None
    }
  }

  val testView = MainBusinessActivityView(sicCodeId_1.id)

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(mainBusinessActivity = Some(testView))

    "extract description from s4LVatSicAndCompliance" in {
      MainBusinessActivityView.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with description" in {
      MainBusinessActivityView.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).mainBusinessActivity shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with description" in {
      MainBusinessActivityView.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).mainBusinessActivity shouldBe Some(testView)
    }

  }
}
