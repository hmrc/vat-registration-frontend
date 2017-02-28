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

class VatChargeExpectancySpec extends UnitSpec with VatRegistrationFixture {

  val turnover = 100L
  val vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  val vatChargeExpectancyNo = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO)
  val vatChargeExpectancyYes = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)

  val vatFinancialsWithReclaimTrue = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = vatAccountingPeriod
  )

  val vatFinancialsWithReclaimFalse = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    vatAccountingPeriod = vatAccountingPeriod
  )

  "toApi" should {
    "update VatFinancials with new VatChargeExpectancy with Reclaim false" in {
      ViewModelTransformer[VatChargeExpectancy, VatFinancials]
        .toApi(vatChargeExpectancyNo, vatFinancialsWithReclaimTrue) shouldBe vatFinancialsWithReclaimFalse
    }
    "update VatFinancials with new VatChargeExpectancy with Reclaim true" in {
      ViewModelTransformer[VatChargeExpectancy, VatFinancials]
        .toApi(vatChargeExpectancyYes, vatFinancialsWithReclaimFalse) shouldBe vatFinancialsWithReclaimTrue
    }
  }

  "apply" should {
    val vatScheme = VatScheme(validRegId)

    "convert VatFinancials with vat charge expectancy yes to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimTrue))
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
    }
    "convert VatFinancials with vat charge expectancy no to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimFalse))
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO)
    }
    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe VatChargeExpectancy()
    }
  }
}

