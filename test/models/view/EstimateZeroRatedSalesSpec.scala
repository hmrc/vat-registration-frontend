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
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  val sales = 60000L
  val turnover = 100L
  val estimateZeroRatedSales = EstimateZeroRatedSales(sales)

  val vatFinancials = VatFinancials(
    turnoverEstimate = turnover,
    zeroRatedSalesEstimate = Some(estimateZeroRatedSales.zeroRatedSalesEstimate),
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  )

  "toApi" should {
    "update VatFinancials with new EstimateZeroRatedSales" in {
      val updatedVatFinancials = VatFinancials(
        turnoverEstimate = turnover,
        zeroRatedSalesEstimate = Some(estimateZeroRatedSales.zeroRatedSalesEstimate),
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )
      ViewModelTransformer[EstimateZeroRatedSales, VatFinancials]
        .toApi(estimateZeroRatedSales, vatFinancials) shouldBe updatedVatFinancials
    }
  }

  "apply" should {
    "convert a VatFinancials to a view model" in {
      val vatScheme = VatScheme(id = validRegId, financials = Some(vatFinancials))
      ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vatScheme) shouldBe Some(estimateZeroRatedSales)
    }

    "convert a VatScheme without a VatFinancials to an empty view model" in {
      val vatScheme = VatScheme(id = validRegId)
      ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vatScheme) shouldBe None
    }
  }

}
