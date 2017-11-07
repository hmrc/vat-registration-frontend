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

package models.view.vatFinancials

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import models.api.{VatFinancials, VatScheme}
import models.view.vatFinancials.VatChargeExpectancy.{VAT_CHARGE_NO, VAT_CHARGE_YES}
import models.{ApiModelTransformer, S4LVatFinancials}
import uk.gov.hmrc.play.test.UnitSpec

class VatChargeExpectancySpec extends UnitSpec with VatRegistrationFixture {

  val turnover = 100L
  val accountingPeriods = monthlyAccountingPeriod
  val vatChargeExpectancyNo = VatChargeExpectancy(VAT_CHARGE_NO)
  val vatChargeExpectancyYes = VatChargeExpectancy(VAT_CHARGE_YES)

  val vatFinancialsWithReclaimTrue = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = true,
    accountingPeriods = accountingPeriods
  )

  val vatFinancialsWithReclaimFalse = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    accountingPeriods = accountingPeriods
  )

  "apply" should {
    val vatScheme = VatScheme(testRegId, status = VatRegStatus.draft)

    "convert VatFinancials with vat charge expectancy yes to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimTrue))
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe Some(VatChargeExpectancy(VAT_CHARGE_YES))
    }
    "convert VatFinancials with vat charge expectancy no to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimFalse))
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe Some(VatChargeExpectancy(VAT_CHARGE_NO))
    }
    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[VatChargeExpectancy].toViewModel(vs) shouldBe None
    }
  }

  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(vatChargeExpectancy = Some(validVatChargeExpectancy))

    "extract VatChargeExpectancy from VatFinancials" in {
      VatChargeExpectancy.viewModelFormat.read(s4lVatFinancials) shouldBe Some(validVatChargeExpectancy)
    }

    "update empty VatFinancials with VatChargeExpectancy" in {
      VatChargeExpectancy.viewModelFormat.update(validVatChargeExpectancy, Option.empty[S4LVatFinancials]).
        vatChargeExpectancy shouldBe Some(validVatChargeExpectancy)
    }

    "update non-empty VatFinancials with VatChargeExpectancy" in {
      VatChargeExpectancy.viewModelFormat.update(validVatChargeExpectancy, Some(s4lVatFinancials)).
        vatChargeExpectancy shouldBe Some(validVatChargeExpectancy)
    }
  }
}

