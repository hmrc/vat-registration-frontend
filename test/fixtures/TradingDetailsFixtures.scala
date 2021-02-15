/*
 * Copyright 2021 HM Revenue & Customs
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

import models.api._
import models.{TradingDetails, TradingNameView}

trait TradingDetailsFixtures extends BaseFixture {

  //View models
  val validTradingNameView = TradingNameView(true, Some("Test Trading Name"))
  val validEuTrading = VatEuTrading(selection = false, eoriApplication = None)
  val validTradingName = TradingName(selection = true, tradingName = Some(testTradingName))

  def generateTradingDetails(
                      tradingNameSelection: Boolean = true,
                      tradingName: Option[String] = Some("ACME Ltd."),
                      euGoodsSelection: Boolean = true
                    ): TradingDetails =
    TradingDetails(
      tradingNameView = Some(TradingNameView(
        yesNo = tradingNameSelection,
        tradingName = tradingName
      )),
      euGoods = Some(euGoodsSelection)
    )
}
