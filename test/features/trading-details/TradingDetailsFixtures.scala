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

import features.tradingDetails.models.TradingDetails
import models.api._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.TaxableTurnover
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}

trait TradingDetailsFixtures extends BaseFixture {

  //View models
  val validTradingNameView = TradingNameView("TRADING_NAME_YES", Some("Test Trading Name"))
  val validTaxableTurnover = TaxableTurnover("TAXABLE_YES")
  val validEuGoods = EuGoods(EuGoods.EU_GOODS_YES)
  val validApplyEori = ApplyEori(ApplyEori.APPLY_EORI_YES)

  // Api models
  val validEuTrading = VatEuTrading(selection = false, eoriApplication = None)
  val validTradingName = TradingName(selection = true, tradingName = Some(testTradingName))


  val validEligibilityChoice = VatEligibilityChoice(
    VatEligibilityChoice.NECESSITY_VOLUNTARY,
    vatThresholdPostIncorp = Some(VatThresholdPostIncorp(true, Some(testDate)))
  )

  val validVatTradingDetails = VatTradingDetails(
    tradingName = validTradingName,
    validEuTrading
  )

  val validTradingDetails = TradingDetails(Some(testTradingName), None)

  def tradingDetails(
                      tradingNameSelection: Boolean = true,
                      tradingName: Option[String] = Some("ACME Ltd."),
                      euGoodsSelection: Boolean = true,
                      eoriApplication: Option[Boolean] = Some(true)
                    ): VatTradingDetails = VatTradingDetails(
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
