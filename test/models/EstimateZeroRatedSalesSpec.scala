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
import models.api.{VatAccountingPeriod, VatBankAccount, VatFinancials, VatScheme}
import models.view.EstimateZeroRatedSales
import uk.gov.hmrc.play.test.UnitSpec

class EstimateZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    "update VatFinancials with new EstimateZeroRatedSales" in {

      val estimateZeroRatedSales = EstimateZeroRatedSales(Some(60000L))

      val vatFinancials = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedSalesEstimate = None,
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )

      val updatedVatFinancials = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedSalesEstimate = estimateZeroRatedSales.zeroRatedSalesEstimate,
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )

      estimateZeroRatedSales.toApi(vatFinancials) shouldBe updatedVatFinancials
    }
  }

  "apply" should {
    "convert a VatFinancials to a view model" in {

      val estimateZeroRatedSales = EstimateZeroRatedSales(Some(60000L))

      val vatFinancials = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedSalesEstimate = estimateZeroRatedSales.zeroRatedSalesEstimate,
        reclaimVatOnMostReturns = true,
        vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
      )

      val vatScheme = VatScheme(
        id = validRegId,
        financials = Some(vatFinancials)
      )

      EstimateZeroRatedSales.apply(vatScheme) shouldBe estimateZeroRatedSales
    }
  }

  "apply" should {
    "convert a VatScheme without a VatFinancials to an empty view model" in {

      val vatScheme = VatScheme(
        id = validRegId,
        financials = None
      )

      EstimateZeroRatedSales.apply(vatScheme) shouldBe EstimateZeroRatedSales.empty
    }
  }
}
