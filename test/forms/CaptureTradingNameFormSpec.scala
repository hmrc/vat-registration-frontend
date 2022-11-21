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
import play.api.data.Form
import testHelpers.VatRegSpec

class CaptureTradingNameFormSpec extends VatRegSpec {
  val testForm: Form[String] = CaptureTradingNameForm.form

  "Trading Name form" must {
    def tradingName(word: String = "") = s"test new trading name$word"

    "be valid" when {
      "correct trading name is provided" in {
        val data = Map("captureTradingName" -> Seq(tradingName()))
        testForm.bindFromRequest(data) shouldContainValue tradingName()
      }

      "correct trading name is provided including an invalid word" in {
        CaptureTradingNameForm.invalidNameSet.foreach {
          invalidName =>
            testForm.bind(Map("captureTradingName" -> tradingName(invalidName))) shouldContainValue tradingName(invalidName)
        }
      }
    }

    "be rejected with correct error messages" when {
      "empty trading name is provided" in {
        val data = Map("captureTradingName" -> Seq(""))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("captureTradingName" -> "validation.captureTradingName.missing")
      }

      "invalid trading name is provided" in {
        val data = Map("captureTradingName" -> Seq("@ rhjksh"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("captureTradingName" -> "validation.captureTradingName.invalid")
      }

      "too long trading name is provided" in {
        val data = Map("captureTradingName" -> Seq(List.fill(161)("a").mkString))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("captureTradingName" -> "validation.captureTradingName.maxlen")
      }

      "yes selected but trading name contains an invalid word" in {
        CaptureTradingNameForm.invalidNameSet.foreach {
          invalidName =>
            testForm.bind(Map("captureTradingName" -> invalidName)) shouldHaveErrors Seq("captureTradingName" -> "validation.captureTradingName.invalid")
        }
      }

    }
  }

}
