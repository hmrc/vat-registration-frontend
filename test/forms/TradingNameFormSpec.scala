/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import helpers.FormInspectors._
import models.TradingNameView
import testHelpers.VatRegSpec

class TradingNameFormSpec extends VatRegSpec {
  val testForm = TradingNameForm.form

  "fillWithPrePop" should {
    "return a form populated with the model as the model = Some and answer is true" in {
      val mappingOfForm = Map(
        "value" -> "true",
        "tradingName" -> "testName"
      )
      val tradingNameModel = Some(TradingNameView(true, Some("testName")))

      val res = TradingNameForm.fillWithPrePop(tradingNameModel)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
    "return a form populated with the model as the model = Some and answer is false" in {
      val mappingOfForm = Map(
        "value" -> "false"
      )
      val tradingNameModel = Some(TradingNameView(false, None))

      val res = TradingNameForm.fillWithPrePop(tradingNameModel)
      res.errors mustBe Seq.empty
      res mustBe testForm.bind(mappingOfForm).discardingErrors
    }
    "return a form with nothing populated as there is no model" in {
      val res = TradingNameForm.fillWithPrePop(None)
      res.errors mustBe Seq.empty
      res mustBe testForm
    }
  }

  "Trading Name form" must {
    def tradingName(word: String = "") = s"test new trading name$word"

    "be valid" when {
      "no is selected" in {
        val data = Map("value" -> Seq("false"))
        testForm.bindFromRequest(data) shouldContainValue(false, None)
      }

      "no is selected and a correct trading name is provided" in {
        val data = Map("value" -> Seq("false"), "tradingName" -> Seq(tradingName()))
        testForm.bindFromRequest(data) shouldContainValue(false, None)
      }

      "yes is selected and a correct trading name is provided" in {
        val data = Map("value" -> Seq("true"), "tradingName" -> Seq(tradingName()))
        testForm.bindFromRequest(data) shouldContainValue(true, Some(tradingName()))
      }

      "yes is selected and a correct trading name is provided including an invalid word" in {
        TradingNameForm.invalidNameSet.foreach{
          invalidName =>
            testForm.bind(Map("value" -> "true", "tradingName" -> tradingName(invalidName))) shouldContainValue(true, Some(tradingName(invalidName)))
        }
      }
    }

    "be rejected with correct error messages" when {
      "no data is provided" in {
        val data = Map[String, Seq[String]]().empty
        testForm.bindFromRequest(data) shouldHaveErrors Seq("value" -> "validation.tradingNameRadio.missing")
      }

      "yes selected but no trading name is provided" in {
        val data = Map("value" -> Seq("true"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "error.required")
      }

      "yes selected but empty trading name is provided" in {
        val data = Map("value" -> Seq("true"), "tradingName" -> Seq(""))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.missing")
      }

      "yes selected but invalid trading name is provided" in {
        val data = Map("value" -> Seq("true"), "tradingName" -> Seq("@ rhjksh"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.invalid")
      }

      "yes selected but too long trading name is provided" in {
        val data = Map("value" -> Seq("true"), "tradingName" -> Seq(List.fill(9)("rh j").mkString))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.maxlen")
      }

      "yes selected but trading name contains an invalid word" in {
        TradingNameForm.invalidNameSet.foreach{
          invalidName =>
            testForm.bind(Map("value" -> "true", "tradingName" -> invalidName)) shouldHaveErrors Seq("tradingName" -> "validation.tradingName.invalid")
        }
      }
    }
  }
}