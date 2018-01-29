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

package models

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api._
import models.view.vatFinancials.ZeroRatedSales.ZERO_RATED_SALES_YES
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class S4LModelsSpec extends UnitSpec with Inspectors with VatRegistrationFixture {

  "S4LVatFinancials.S4LApiTransformer.toApi" should {

    val s4l = S4LVatFinancials(
      zeroRatedTurnover = Some(ZeroRatedSales(ZERO_RATED_SALES_YES)),
      zeroRatedTurnoverEstimate = Some(EstimateZeroRatedSales(1))
    )

    "transform complete S4L model to API" in {
      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4l) shouldBe expected
    }

    "transform valid partial S4L model to API" in {
      val s4lWithoutAccountingPeriod = s4l.copy()

      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4lWithoutAccountingPeriod) shouldBe expected
    }
  }
}

