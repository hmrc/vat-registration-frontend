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
import models.view.ZeroRatedSales
import uk.gov.hmrc.play.test.UnitSpec

class ZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  val vatFinancialsWithZeroRated = VatFinancials(
    Some(VatBankAccount("ACME", "101010","100000000000")),
    100L,
    Some(200L),
    true,
    VatAccountingPeriod(None, "monthly")
  )

  val vatFinancialsWithoutZeroRated = VatFinancials(
    Some(VatBankAccount("ACME", "101010","100000000000")),
    100L,
    None,
    true,
    VatAccountingPeriod(None, "monthly")
  )

  val vatScheme = validVatScheme.copy(financials = Some(vatFinancialsWithoutZeroRated))

  "empty" should {
    "create an empty Zero Rated Sales model" in {
      ZeroRatedSales.empty shouldBe ZeroRatedSales("")
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatFinancials API model with ZeroRatedSales to view model" in {
      val vatScheme = validVatScheme.copy(financials = Some(vatFinancialsWithZeroRated))
      ZeroRatedSales.apply(vatScheme) shouldBe ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_YES)
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatFinancials API model without ZeroRatedSales to view model" in {
      val vatScheme = validVatScheme.copy(financials = Some(vatFinancialsWithoutZeroRated))
      ZeroRatedSales.apply(vatScheme) shouldBe ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO)
    }
  }
}

