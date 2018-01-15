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

class ZeroRatedSalesSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    val vatScheme = VatScheme(testRegId, status = VatRegStatus.draft)

    "convert VatFinancials with zero rated sales to view model" in {
      val vatFinancialsWithZeroRated = VatFinancials(
        turnoverEstimate = 100L,
        zeroRatedTurnoverEstimate = Some(200L)
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithZeroRated))
      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe Some(ZeroRatedSales.yes)
    }

    "convert VatFinancials without zero rated sales to view model" in {
      val vatFinancialsWithoutZeroRated = VatFinancials(
        turnoverEstimate = 100L
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithoutZeroRated))

      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe Some(ZeroRatedSales.no)
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[ZeroRatedSales].toViewModel(vs) shouldBe None
    }
  }

  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(zeroRatedTurnover = Some(ZeroRatedSales.yes))

    "extract ZeroRatedSales from VatFinancials" in {
      ZeroRatedSales.viewModelFormat.read(s4lVatFinancials) shouldBe Some(ZeroRatedSales.yes)
    }

    "update empty VatFinancials with ZeroRatedSales" in {
      ZeroRatedSales.viewModelFormat.update(ZeroRatedSales.yes, Option.empty[S4LVatFinancials]).
        zeroRatedTurnover shouldBe Some(ZeroRatedSales.yes)
    }

    "update non-empty VatFinancials with ZeroRatedSales" in {
      ZeroRatedSales.viewModelFormat.update(ZeroRatedSales.yes, Some(s4lVatFinancials)).
        zeroRatedTurnover shouldBe Some(ZeroRatedSales.yes)
    }
  }
}
