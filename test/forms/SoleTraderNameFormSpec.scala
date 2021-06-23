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

package forms

import helpers.FormInspectors._
import testHelpers.VatRegSpec

class SoleTraderNameFormSpec extends VatRegSpec {
  val testForm = SoleTraderNameForm.form

  "Trading Name form" must {
    def tradingName(word: String = "") = s"test new trading name$word"

    "be valid" when {
      "correct trading name is provided" in {
        val data = Map("trading-name" -> Seq(tradingName()))
        testForm.bindFromRequest(data) shouldContainValue tradingName()
      }

      "correct trading name is provided including an invalid word" in {
        SoleTraderNameForm.invalidNameSet.foreach {
          invalidName =>
            testForm.bind(Map("trading-name" -> tradingName(invalidName))) shouldContainValue tradingName(invalidName)
        }
      }
    }

    "be rejected with correct error messages" when {
      "no data is provided" in {
        val data = Map[String, Seq[String]]().empty
        testForm.bindFromRequest(data) shouldHaveErrors Seq("trading-name" -> "error.required")
      }

      "invalid trading name is provided" in {
        val data = Map("trading-name" -> Seq("@ trash"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("trading-name" -> "validation.trading-name.invalid")
      }
    }
  }

}
