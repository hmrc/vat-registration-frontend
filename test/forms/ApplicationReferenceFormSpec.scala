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

import testHelpers.VatRegSpec

class ApplicationReferenceFormSpec extends VatRegSpec {

  val form = app.injector.instanceOf[ApplicationReferenceForm]

  val validValues = Seq (
    ("letters", "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    ("numbers", "1234567890"),
    ("special characters", " '‘()[]{}<>!«»\"ʺ˝ˮ?/\\\\+=%#*&$€£_-@¥.,:;")
  )

  "the Application Reference form" must {
    validValues.collect { case (test, value) =>
      s"bind valid characters for $test" in {
        form().bind(Map("value" -> value)).errors.isEmpty mustBe true
      }
    }

    "show an error message if the form is empty" in {
      form().bind(Map("value" -> "")).errors.headOption.map(_.message) mustBe Some("applicationReference.error.missing")
    }

    "show an error message if the value is invalid" in {
      form().bind(Map("value" -> "~")).errors.headOption.map(_.message) mustBe Some("applicationReference.error.invalid")
    }

    "show an error message if the value is too long" in {
      form().bind(Map("value" -> "w" * 101)).errors.headOption.map(_.message) mustBe Some("applicationReference.error.length")
    }
  }

}
