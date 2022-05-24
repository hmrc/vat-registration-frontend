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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class BusinessEmailAddressFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val businessEmailForm: Form[String] = BusinessEmailAddressForm.form
  val testEmail: String = "test@test.com"
  val incorrect_email_format_error_key: String = "validation.businessEmail.incorrectFormat"
  val email_length_error_key: String = "validation.businessEmail.tooMany"
  val email_empty_error_key: String = "validation.businessEmail.noEntry"

  "The businessEmailForm" must {
    "validate that testEmail is valid" in {
      val form = businessEmailForm.bind(Map(BusinessEmailAddressForm.businessEmailKey -> testEmail)).value

      form mustBe Some(testEmail)
    }

    "validate that incorrect email format fails" in {
      val formWithError = businessEmailForm.bind(Map(BusinessEmailAddressForm.businessEmailKey -> "invalid"))

      formWithError.errors must contain(FormError(BusinessEmailAddressForm.businessEmailKey, incorrect_email_format_error_key))
    }

    "validate that an email exceeding max length fails" in {
      val exceedMaxLengthEmail: String = ("a" * 132) + "@test.com"
      val formWithError = businessEmailForm.bind(Map(BusinessEmailAddressForm.businessEmailKey -> exceedMaxLengthEmail))

      formWithError.errors must contain(FormError(BusinessEmailAddressForm.businessEmailKey, email_length_error_key))
    }

    "validate that an empty field fails" in {
      val formWithError = businessEmailForm.bind(Map(BusinessEmailAddressForm.businessEmailKey -> ""))

      formWithError.errors must contain(FormError(BusinessEmailAddressForm.businessEmailKey, email_empty_error_key))
    }

  }

}
