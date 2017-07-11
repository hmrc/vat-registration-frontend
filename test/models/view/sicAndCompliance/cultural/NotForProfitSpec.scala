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

package models.view.sicAndCompliance.cultural

import fixtures.VatRegistrationFixture
import models.api.{SicCode, VatComplianceCultural, VatSicAndCompliance}
import models.view.sicAndCompliance.cultural.NotForProfit.NOT_PROFIT_NO
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class NotForProfitSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    val notForProfit = NotForProfit(NOT_PROFIT_NO)

    val vatSicAndCompliance = VatSicAndCompliance(
      businessDescription = businessActivityDescription,
      culturalCompliance = Some(VatComplianceCultural(true)),
      mainBusinessActivity = SicCode("","","")
    )

    val differentSicAndCompliance = VatSicAndCompliance(
      businessDescription = businessActivityDescription,
      culturalCompliance = Some(VatComplianceCultural(false)),
      mainBusinessActivity = SicCode("","","")
    )

    "update VatSicAndCompliance with new NotForProfit" in {
      ViewModelTransformer[NotForProfit, VatSicAndCompliance]
        .toApi(notForProfit, vatSicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[NotForProfit].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without CulturalCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(culturalComplianceSection = None, mainBusinessActivitySection = sicCode)))
      ApiModelTransformer[NotForProfit].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with CulturalCompliance section to view model - for profit" in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            culturalComplianceSection = Some(VatComplianceCultural(notForProfit = false)))))
      ApiModelTransformer[NotForProfit].toViewModel(vs) shouldBe Some(NotForProfit(NotForProfit.NOT_PROFIT_NO))
    }

    "convert VatScheme with CulturalCompliance section to view model - not for profit" in {
      val vs = vatScheme(
        sicAndCompliance = Some(
          vatSicAndCompliance(mainBusinessActivitySection = sicCode,
            culturalComplianceSection = Some(VatComplianceCultural(notForProfit = true)))))
      ApiModelTransformer[NotForProfit].toViewModel(vs) shouldBe Some(NotForProfit(NotForProfit.NOT_PROFIT_YES))
    }

  }

  val testView = NotForProfit("yes")

  "ViewModelFormat" should {
    val s4LVatSicAndCompliance = S4LVatSicAndCompliance(notForProfit = Some(testView))

    "extract notForProfit from s4LVatSicAndCompliance" in {
      NotForProfit.viewModelFormat.read(s4LVatSicAndCompliance) shouldBe Some(testView)
    }

    "update empty s4LVatSicAndCompliance with notForProfit" in {
      NotForProfit.viewModelFormat.update(testView, Option.empty[S4LVatSicAndCompliance]).notForProfit shouldBe Some(testView)
    }

    "update non-empty s4LVatSicAndCompliance with notForProfit" in {
      NotForProfit.viewModelFormat.update(testView, Some(s4LVatSicAndCompliance)).notForProfit shouldBe Some(testView)
    }
  }
}

