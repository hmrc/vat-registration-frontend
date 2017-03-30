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
import models.view.vatFinancials.EstimateVatTurnover
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateVatTurnoverSpec extends UnitSpec with VatRegistrationFixture {
  val turnover = 5000L
  val turnoverNew = 1000L
  val estimatedVatTurnover = EstimateVatTurnover(turnover)
  val newEstimateVatTurnover = EstimateVatTurnover(turnoverNew)

  val vatFinancials = VatFinancials(
    turnoverEstimate = estimatedVatTurnover.vatTurnoverEstimate,
    reclaimVatOnMostReturns = true,
    accountingPeriods = monthlyAccountingPeriod
  )
  val differentVatFinancials = VatFinancials(
    turnoverEstimate = newEstimateVatTurnover.vatTurnoverEstimate,
    reclaimVatOnMostReturns = true,
    accountingPeriods = monthlyAccountingPeriod
  )
  val vatScheme = VatScheme(id = validRegId, financials = Some(vatFinancials))

  "toApi" should {
    "update a VatFinancials with new EstimateVatTurnover" in {
      ViewModelTransformer[EstimateVatTurnover, VatFinancials]
        .toApi(newEstimateVatTurnover, vatFinancials) shouldBe differentVatFinancials
    }
  }

  "apply" should {
    "Extract a EstimateVatTurnover view model from a VatScheme" in {
      ApiModelTransformer[EstimateVatTurnover].toViewModel(vatScheme) shouldBe Some(estimatedVatTurnover)
    }
    "Extract an empty EstimateVatTurnover view model from a VatScheme without financials" in {
      val vatSchemeWithoutFinancials = VatScheme(id = validRegId, financials = None)
      ApiModelTransformer[EstimateVatTurnover].toViewModel(vatSchemeWithoutFinancials) shouldBe None
    }
  }
}
