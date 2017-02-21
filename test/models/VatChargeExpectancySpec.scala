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
import models.view.{EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
import uk.gov.hmrc.play.test.UnitSpec

class VatChargeExpectancySpec extends UnitSpec with VatRegistrationFixture {

  val validVatChargeExpectancy = VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO)

  val vatFinancialsWithReclaimTrue = VatFinancials(
    turnoverEstimate = 100L,
    zeroRatedSalesEstimate = Some(200L),
    reclaimVatOnMostReturns = true,
    vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  )

  val vatFinancialsWithReclaimFalse = VatFinancials(
    turnoverEstimate = 100L,
    zeroRatedSalesEstimate = Some(200L),
    reclaimVatOnMostReturns = false,
    vatAccountingPeriod = VatAccountingPeriod(None, "monthly")
  )

  override val validVatFinancials = VatFinancials(Some(VatBankAccount("ACME", "101010","100000000000")),
    10000000000L,
    validEstimateZeroRatedSales.zeroRatedSalesEstimate,
    true,
    VatAccountingPeriod(None, "monthly")
  )

  val differentVatFinancials = VatFinancials(Some(VatBankAccount("ACME", "101010","100000000000")),
    10000000000L,
    validEstimateZeroRatedSales.zeroRatedSalesEstimate,
    false,
    VatAccountingPeriod(None, "monthly")
  )

  override val validVatScheme = VatScheme(
    validRegId,
    Some(validVatTradingDetails),
    Some(validVatChoice),
    Some(validVatFinancials)
  )

  val vatScheme = VatScheme(validRegId)

  "toApi" should {
    "upserts (merge) a current VatFinancials API model with the details of an instance of EstimateZeroRatedSales view model" in {
      validVatChargeExpectancy.toApi(validVatFinancials) shouldBe differentVatFinancials
    }
  }

  "empty" should {
    "create an empty Vat Charge Expectancy model" in {
      VatChargeExpectancy.empty shouldBe VatChargeExpectancy("")
    }
  }

  "apply" should {

    "convert VatFinancials with vat charge expectancy yes to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimTrue))
      VatChargeExpectancy.apply(vs) shouldBe VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_YES)
    }

    "convert VatFinancials with vat charge expectancy no to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithReclaimFalse))
      VatChargeExpectancy.apply(vs) shouldBe VatChargeExpectancy(VatChargeExpectancy.VAT_CHARGE_NO)
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      VatChargeExpectancy.apply(vs) shouldBe VatChargeExpectancy.empty
    }

  }

}

