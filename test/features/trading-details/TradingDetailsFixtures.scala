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

package fixtures

import java.time.LocalDate

import models.api._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}

trait TradingDetailsFixture extends BaseFixture {
  // Test variables
  val testTradingName = "ACME INC"

  //View models
  val validTradingNameView = TradingNameView("TRADING_NAME_YES", Some("Test Trading Name"))
  val validStartDateView = StartDateView(StartDateView.SPECIFIC_DATE, Some(testDate))
  val validTaxableTurnover = TaxableTurnover("TAXABLE_YES")
  val validEuGoods = EuGoods(EuGoods.EU_GOODS_YES)
  val validApplyEori = ApplyEori(ApplyEori.APPLY_EORI_YES)

  // Api models
  val vatStartDate = VatStartDate(StartDateView.SPECIFIC_DATE, Some(testDate))
  val validEuTrading = VatEuTrading(selection = false, eoriApplication = None)
  val validTradingName = TradingName(selection = true, tradingName = Some(testTradingName))

  val validVatChoice = VatChoice(
    VatChoice.NECESSITY_VOLUNTARY,
    vatStartDate,
    vatThresholdPostIncorp = Some(VatThresholdPostIncorp(true, Some(testDate)))
  )

  val validVatTradingDetails = VatTradingDetails(
    vatChoice = validVatChoice,
    tradingName = validTradingName,
    validEuTrading
  )

  def tradingDetails(
                      necessity: String = VatChoice.NECESSITY_VOLUNTARY,
                      startDateSelection: String = StartDateView.COMPANY_REGISTRATION_DATE,
                      startDate: Option[LocalDate] = None,
                      tradingNameSelection: Boolean = true,
                      tradingName: Option[String] = Some("ACME Ltd."),
                      reason: Option[String] = None,
                      euGoodsSelection: Boolean = true,
                      eoriApplication: Option[Boolean] = Some(true)
                    ): VatTradingDetails = VatTradingDetails(
    vatChoice = VatChoice(
      necessity = necessity,
      vatStartDate = VatStartDate(
        selection = startDateSelection,
        startDate = startDate
      ),
      reason = reason
    ),
    tradingName = TradingName(
      selection = tradingNameSelection,
      tradingName = tradingName
    ),
    euTrading = VatEuTrading(
      euGoodsSelection,
      eoriApplication
    )
  )
}