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

package models.view.vatFinancials

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, S4LVatFinancials}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  val sales = 60000L
  val turnover = 100L
  val estimateZeroRatedSales = EstimateZeroRatedSales(sales)

  val vatFinancials = VatFinancials(
    turnoverEstimate = turnover,
    zeroRatedTurnoverEstimate = Some(estimateZeroRatedSales.zeroRatedTurnoverEstimate)
  )

  "apply" should {
    "convert a VatFinancials to a view model" in {
      val vatScheme = VatScheme(id = testRegId, status = VatRegStatus.draft, financials = Some(vatFinancials))
      ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vatScheme) shouldBe Some(estimateZeroRatedSales)
    }

    "convert a VatScheme without a VatFinancials to an empty view model" in {
      val vatScheme = VatScheme(id = testRegId, status = VatRegStatus.draft)
      ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vatScheme) shouldBe None
    }
  }

  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales))

    "extract EstimateZeroRatedSales from VatFinancials" in {
      EstimateZeroRatedSales.viewModelFormat.read(s4lVatFinancials) shouldBe Some(validEstimateZeroRatedSales)
    }

    "update empty VatFinancials with EstimateZeroRatedSales" in {
      EstimateZeroRatedSales.viewModelFormat.update(validEstimateZeroRatedSales, Option.empty[S4LVatFinancials]).
        zeroRatedTurnoverEstimate shouldBe Some(validEstimateZeroRatedSales)
    }

    "update non-empty VatFinancials with EstimateZeroRatedSales" in {
      EstimateZeroRatedSales.viewModelFormat.update(validEstimateZeroRatedSales, Some(s4lVatFinancials)).
        zeroRatedTurnoverEstimate shouldBe Some(validEstimateZeroRatedSales)
    }
  }

}
