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

package models.view.vatFinancials.vatAccountingPeriod

import fixtures.VatRegistrationFixture
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.{MONTHLY, QUARTERLY}
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class VatReturnFrequencySpec extends UnitSpec with VatRegistrationFixture {

  val VatReturnFrequencyWithMonthly = VatFinancials(
    turnoverEstimate = 0L,
    zeroRatedTurnoverEstimate = Some(0L),
    reclaimVatOnMostReturns = true,
    accountingPeriods = VatAccountingPeriod(frequency = MONTHLY)
  )

  val VatReturnFrequencyWithQuarterly = VatFinancials(
    turnoverEstimate = 0L,
    zeroRatedTurnoverEstimate = Some(0L),
    reclaimVatOnMostReturns = true,
    accountingPeriods = VatAccountingPeriod(frequency = QUARTERLY)
  )

  val vatScheme = VatScheme(validRegId)

  "toApi" should {
    "update VatFinancials with new VatReturnFrequency" in {

      val vatReturnFrequency = VatReturnFrequency(MONTHLY)

      val vatFinancials = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(frequency = MONTHLY)
      )

      val updatedVatFinancials = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedTurnoverEstimate = None,
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod(frequency = MONTHLY)
      )
      ViewModelTransformer[VatReturnFrequency, VatFinancials]
        .toApi(vatReturnFrequency, vatFinancials) shouldBe updatedVatFinancials
    }
  }

  "apply" should {
    "convert VatFinancials with MONTHLY vat return frequency to view model" in {
      val vs = vatScheme.copy(financials = Some(VatReturnFrequencyWithMonthly))
      ApiModelTransformer[VatReturnFrequency].toViewModel(vs) shouldBe Some(VatReturnFrequency(MONTHLY))
    }

    "convert VatFinancials with QUARTERLY vat return frequency to view model" in {
      val vs = vatScheme.copy(financials = Some(VatReturnFrequencyWithQuarterly))
      ApiModelTransformer[VatReturnFrequency].toViewModel(vs) shouldBe Some(VatReturnFrequency(QUARTERLY))
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[VatReturnFrequency].toViewModel(vs) shouldBe None
    }
  }

}

