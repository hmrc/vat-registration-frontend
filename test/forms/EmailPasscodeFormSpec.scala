/*
 * Copyright 2020 HM Revenue & Customs
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
  val invalid_format_error_key: String = "capture-email-passcode.error.incorrect_passcode"
  val passcode_not_entered_error_key: String = "capture-email-passcode.error.nothing_entered"

  "The passcodeForm" should {
    "validate that testPasscode is valid" in {
      val form = passcodeForm.bind(Map(EmailPasscodeForm.passcodeKey -> testPasscode)).value

      form shouldBe Some(testPasscode)
    }

    "validate that data has been entered" in {
      val formWithError = passcodeForm.bind(Map(EmailPasscodeForm.passcodeKey -> ""))

      formWithError.errors should contain(FormError(EmailPasscodeForm.passcodeKey, passcode_not_entered_error_key))
    }
  }

}
