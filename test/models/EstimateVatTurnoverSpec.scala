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
import models.view.{EstimateVatTurnover, TradingName, ZeroRatedSales}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateVatTurnoverSpec extends UnitSpec with VatRegistrationFixture {
  val turnover = 5000L
  val turnoverNew = 1000L
  val estimatedVatTurnover = EstimateVatTurnover(Some(turnover))
  val newEstimateVatTurnover = EstimateVatTurnover(Some(turnoverNew))

  val vatFinancials = VatFinancials(
    turnoverEstimate = estimatedVatTurnover.vatTurnoverEstimate.get,
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  )
  val differentVatFinancials = VatFinancials(
    turnoverEstimate = newEstimateVatTurnover.vatTurnoverEstimate.get,
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  )
  val vatScheme = VatScheme(id = validRegId, financials = Some(vatFinancials))

  "empty" should {
    "create an empty Zero Rated Sales model" in {
      EstimateVatTurnover.empty shouldBe EstimateVatTurnover(None)
    }
  }

  "toApi" should {
    "update a VatFinancials with new EstimateVatTurnover" in {

      newEstimateVatTurnover.toApi(vatFinancials) shouldBe differentVatFinancials
    }
  }

  "apply" should {
    "Extract a EstimateVatTurnover view model from a VatScheme" in {
      EstimateVatTurnover.apply(vatScheme) shouldBe estimatedVatTurnover
    }
    "Extract an empty EstimateVatTurnover view model from a VatScheme without financials" in {
      val vatSchemeWithoutFinancials = VatScheme(id = validRegId, financials = None)
      EstimateVatTurnover.apply(vatSchemeWithoutFinancials) shouldBe EstimateVatTurnover.empty
    }
  }
}
