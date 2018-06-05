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

package features.tradingDetails.forms

import features.tradingDetails.TradingNameView
import forms.TradingNameForm
import helpers.VatRegSpec
import helpers.FormInspectors._

class TradingNameFormSpec extends VatRegSpec {
  val testForm = TradingNameForm.form

  "fillWithPrePop" should {
    "return a form with just trading Name populated and differentName set to blank string if TradingName model = None" in {
      val mappingOfForm = Map(
        "tradingNameRadio" -> "",
        "tradingName" -> "foo bar wizz pre pop"
      )
      val prePopName = Some("foo bar wizz pre pop")
      val tradingNameModel = None

      val res = TradingNameForm.fillWithPrePop(prePopName,tradingNameModel)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
    "return a form populated with the model not the pre pop trading name as the model = Some and answer is true" in {
      val mappingOfForm = Map(
        "tradingNameRadio" -> "true",
        "tradingName" -> "foo"
      )
      val prePopName = Some("foo bar wizz pre pop")
      val tradingNameModel = Some(TradingNameView(true,Some("foo")))

      val res = TradingNameForm.fillWithPrePop(prePopName,tradingNameModel)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
    "return a form populated with the model and the pre pop trading name as the model = Some and answer is false" in {
      val mappingOfForm = Map(
        "tradingNameRadio" -> "false",
        "tradingName" -> "foo bar wizz pre pop"
      )
      val prePopName = Some("foo bar wizz pre pop")
      val tradingNameModel = Some(TradingNameView(false,None))

      val res = TradingNameForm.fillWithPrePop(prePopName,tradingNameModel)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
    "return a form with nothing populated as there is neither pre pop nor is there a model" in {
      val mappingOfForm = Map(
        "tradingNameRadio" -> "",
        "tradingName" -> "")

      val res = TradingNameForm.fillWithPrePop(None,None)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
  }
  "Trading Name form" must {
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