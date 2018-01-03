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
  val vatScheme = VatScheme(id = testRegId, status = VatRegStatus.draft, financials = Some(vatFinancials))

  "apply" should {
    "Extract a EstimateVatTurnover view model from a VatScheme" in {
      ApiModelTransformer[EstimateVatTurnover].toViewModel(vatScheme) shouldBe Some(estimatedVatTurnover)
    }
    "Extract an empty EstimateVatTurnover view model from a VatScheme without financials" in {
      val vatSchemeWithoutFinancials = VatScheme(id = testRegId, status = VatRegStatus.draft,  financials = None)
      ApiModelTransformer[EstimateVatTurnover].toViewModel(vatSchemeWithoutFinancials) shouldBe None
    }
  }

  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(estimateVatTurnover = Some(validEstimateVatTurnover))

    "extract EstimateVatTurnover from VatFinancials" in {
      EstimateVatTurnover.viewModelFormat.read(s4lVatFinancials) shouldBe Some(validEstimateVatTurnover)
    }

    "update empty VatFinancials with EstimateVatTurnover" in {
      EstimateVatTurnover.viewModelFormat.update(validEstimateVatTurnover, Option.empty[S4LVatFinancials]).
        estimateVatTurnover shouldBe Some(validEstimateVatTurnover)
    }

    "update non-empty VatFinancials with EstimateVatTurnover" in {
      EstimateVatTurnover.viewModelFormat.update(validEstimateVatTurnover, Some(s4lVatFinancials)).
        estimateVatTurnover shouldBe Some(validEstimateVatTurnover)
    }
  }
}
