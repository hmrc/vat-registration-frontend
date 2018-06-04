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

package features.tradingDetails

import forms.TradingNameForm
import uk.gov.hmrc.play.test.UnitSpec
import helpers.FormInspectors._

class TradingDetailsFormsSpec extends UnitSpec {
  "Trading Name form" must {
    val testForm = TradingNameForm.form
    val tradingName = "test new trading name"

    "be valid" when {
      "no is selected" in {
        val data = Map("tradingNameRadio" -> Seq("false"))
        testForm.bindFromRequest(data) shouldContainValue (false, None)
      }

      "no is selected and a correct trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("false"), "tradingName" -> Seq(tradingName))
        testForm.bindFromRequest(data) shouldContainValue (false, None)
      }

      "yes is selected and a correct trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("true"), "tradingName" -> Seq(tradingName))
        testForm.bindFromRequest(data) shouldContainValue (true, Some(tradingName))
      }
    }

    "be rejected with correct error messages" when {
      "no data is provided" in {
        val data = Map[String, Seq[String]]().empty
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingNameRadio" -> "validation.tradingNameRadio.missing")
      }

      "yes selected but no trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("true"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "error.required")
      }

      "yes selected but empty trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("true"), "tradingName" -> Seq(""))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.missing")
      }

      "yes selected but invalid trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("true"), "tradingName" -> Seq("@ rhjksh"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.invalid")
      }

      "yes selected but too long trading name is provided" in {
        val data = Map("tradingNameRadio" -> Seq("true"), "tradingName" -> Seq(List.fill(9)("rh j").mkString))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.maxlen")
      }
    }
  }
}