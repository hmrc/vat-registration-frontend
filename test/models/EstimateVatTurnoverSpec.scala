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
import models.api.{VatAccountingPeriod, VatBankAccount, VatFinancials}
import models.view.{EstimateVatTurnover, TradingName}
import uk.gov.hmrc.play.test.UnitSpec

class EstimateVatTurnoverSpec extends UnitSpec with VatRegistrationFixture {
  override val validEstimateVatTurnover = EstimateVatTurnover(Some(50000L))
  override val differentEstimateVatTurnover = EstimateVatTurnover(Some(10000L))

  override val validVatFinancials = VatFinancials(Some(VatBankAccount("ACME", "101010","100000000000")),
    validEstimateVatTurnover.vatTurnoverEstimate.get,
    Some(10000000000L),
    true,
    VatAccountingPeriod(None, "monthly")
  )

  val differentVatFinancials = VatFinancials(Some(VatBankAccount("ACME", "101010","100000000000")),
    differentEstimateVatTurnover.vatTurnoverEstimate.get,
    Some(10000000000L),
    true,
    VatAccountingPeriod(None, "monthly")
  )


  "toApi" should {
    "upserts (merge) a current VatFinancials API model with the details of an instance of EstimateVatTurnover view model" in {
      differentEstimateVatTurnover.toApi(validVatFinancials) shouldBe differentVatFinancials
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatFinancials API model to an instance of EstimateVatTurnover view model" in {
      EstimateVatTurnover.apply(validVatScheme) shouldBe EstimateVatTurnover(Some(10000000000L))
    }
  }
}
