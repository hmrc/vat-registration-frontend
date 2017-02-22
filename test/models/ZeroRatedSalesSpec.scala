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

package models

import fixtures.VatRegistrationFixture
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.ZeroRatedSales
import uk.gov.hmrc.play.test.UnitSpec

class ZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty Zero Rated Sales model" in {
      ZeroRatedSales.empty shouldBe ZeroRatedSales("")
    }
  }

  "apply" should {
    val vatScheme = VatScheme(validRegId)

    "convert VatFinancials with zero rated sales to view model" in {
      val vatFinancialsWithZeroRated = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedSalesEstimate = Some(200L),
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithZeroRated))

      ZeroRatedSales.apply(vs) shouldBe ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_YES)
    }

    "convert VatFinancials without zero rated sales to view model" in {
      val vatFinancialsWithoutZeroRated = VatFinancials(
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithoutZeroRated))

      ZeroRatedSales.apply(vs) shouldBe ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO)
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ZeroRatedSales.apply(vs) shouldBe ZeroRatedSales.empty
    }
  }
}

