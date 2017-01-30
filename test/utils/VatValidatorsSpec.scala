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

package utils

import forms.vatDetails.TradingNameForm
import helpers.VatRegSpec
import models.view.TradingName
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class VatValidatorsSpec extends VatRegSpec {

    val testTradingNameForm = TradingNameForm.form

    "return an error when user enters a empty trading name and selected Yes " in {
      val data : Map[String, String] =
        Map(
          "tradingName.yesNo" -> TradingName.TRADING_NAME_YES,
          "tradingName" -> ""
        )

      val boundForm = testTradingNameForm.bind(data)
      boundForm.errors.map(err => (err.key, err.message)) mustBe  List(("", VatValidators.EMPTY_TRADING_NAME_MSG_KEY))

    }


  "return an error when user enters a Invalid trading name and selected Yes " in {
    val data : Map[String, String] =
      Map(
        "tradingName.yesNo" -> TradingName.TRADING_NAME_YES,
        "tradingName" -> "££££$$$$$"
      )

    val boundForm = testTradingNameForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("",  VatValidators.IN_VALID_TRADING_NAME_MSG_KEY))

  }

  "return success when user selected No " in {
    val data : Map[String, String] =
      Map(
        "tradingName.yesNo" -> TradingName.TRADING_NAME_NO,
        "tradingName" -> ""
      )

    val boundForm = testTradingNameForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List()

  }
}
