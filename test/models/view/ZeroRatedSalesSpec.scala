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
import models.ApiModelTransformer
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.vatFinancials.ZeroRatedSales
import models.view.vatFinancials.ZeroRatedSales.{ZERO_RATED_SALES_NO, ZERO_RATED_SALES_YES}
import uk.gov.hmrc.play.test.UnitSpec

class ZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    val vatScheme = VatScheme(validRegId)

    "convert VatFinancials with zero rated sales to view model" in {
      val vatFinancialsWithZeroRated = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedTurnoverEstimate = Some(200L),
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithZeroRated))
      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe Some(ZeroRatedSales(ZERO_RATED_SALES_YES))
    }

    "convert VatFinancials without zero rated sales to view model" in {
      val vatFinancialsWithoutZeroRated = VatFinancials(
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithoutZeroRated))

      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe Some(ZeroRatedSales(ZERO_RATED_SALES_NO))
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe None
    }
  }
}

