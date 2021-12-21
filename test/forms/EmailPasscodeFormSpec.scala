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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class EmailPasscodeFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val passcodeForm: Form[String] = EmailPasscodeForm.form
  val testPasscode: String = "ABCDEF"
  val maxPasscodeLength: Int = 6
  val incorrect_format_error_key: String = "capture-email-passcode.error.incorrect_passcode"
  val incorrect_length_error_key: String = "capture-email-passcode.error.incorrect_length"

  "The passcodeForm" should {
    "validate that testPasscode is valid" in {
      val form = passcodeForm.bind(Map(EmailPasscodeForm.passcodeKey -> testPasscode)).value

      form shouldBe Some(testPasscode)
    }

    "validate that the field is not empty" in {
      val formWithError = passcodeForm.bind(Map(EmailPasscodeForm.passcodeKey -> ""))

      formWithError.errors should contain(FormError(EmailPasscodeForm.passcodeKey, incorrect_length_error_key))
    }

    "validate that the passcode is not greater than 6 characters" in {
      val formWithError = passcodeForm.bind(Map(EmailPasscodeForm.passcodeKey -> "1234567"))

      formWithError.errors should contain(FormError(EmailPasscodeForm.passcodeKey, incorrect_length_error_key))
    }
  }

}
