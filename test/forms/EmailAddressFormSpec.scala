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

class EmailAddressFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val emailForm: Form[String] = EmailAddressForm.form
  val testEmail: String = "test@test.com"
  val incorrect_email_format_error_key: String = "capture-email-address.error.incorrect_format"
  val email_length_error_key: String = "capture-email-address.error.incorrect_length"
  val email_empty_error_key: String = "capture-email-address.error.nothing_entered"

  "The emailForm" should {
    "validate that testEmail is valid" in {
      val form = emailForm.bind(Map(EmailAddressForm.emailKey -> testEmail)).value

      form shouldBe Some(testEmail)
    }

    "validate that incorrect email format fails" in {
      val formWithError = emailForm.bind(Map(EmailAddressForm.emailKey -> "invalid"))

      formWithError.errors should contain(FormError(EmailAddressForm.emailKey, incorrect_email_format_error_key))
    }

    "validate that an email exceeding max length fails" in {
      val exceedMaxLengthEmail: String = ("a" * 132) + "@test.com"
      val formWithError = emailForm.bind(Map(EmailAddressForm.emailKey -> exceedMaxLengthEmail))

      formWithError.errors should contain(FormError(EmailAddressForm.emailKey, email_length_error_key))
    }

    "validate that an empty field fails" in {
      val formWithError = emailForm.bind(Map(EmailAddressForm.emailKey -> ""))

      formWithError.errors should contain(FormError(EmailAddressForm.emailKey, email_empty_error_key))
    }

  }

}
