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

package fixtures

import models.S4LVatFinancials
import models.api.{VatBankAccount, VatFinancials}
import models.view.vatFinancials.ZeroRatedSales.ZERO_RATED_SALES_YES
import models.view.vatFinancials._

trait FinancialsFixtures extends BaseFixture {

  //Test variables
  val testTurnoverEstimate = 50000L
  val testEstimatedSales = 60000L

  //View models
  val validEstimateZeroRatedSales = EstimateZeroRatedSales(testEstimatedSales)

  //Api models
  val validVatBankAccount = VatBankAccount(testTradingName, testAccountNumber, testSortCode)

  val validVatFinancials = VatFinancials(
    zeroRatedTurnoverEstimate = Some(testEstimatedSales)
  )

  val validS4LVatFinancials = S4LVatFinancials(
    zeroRatedTurnover = Some(ZeroRatedSales(ZERO_RATED_SALES_YES)),
    zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales)
  )
}
