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

class TelephoneNumberFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val telephoneNumberForm: Form[String] = TelephoneNumberForm.form
  val testTelephoneNumber: String = "1234"
  val incorrect_telephone_number_format_error_key: String = "capture-telephone-number.error.incorrect_format"
  val telephone_number_length_error_key: String = "capture-telephone-number.error.incorrect_length"
  val telephone_number_empty_error_key: String = "capture-telephone-number.error.nothing_entered"

  "The telephoneNumberForm" should {
    "validate that testTelephoneNumber is valid" in {
      val form = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> testTelephoneNumber)).value

      form shouldBe Some(testTelephoneNumber)
    }

    "validate that incorrect telephone number format fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> ":::><|"))

      formWithError.errors should contain(FormError(TelephoneNumberForm.telephoneNumberKey, incorrect_telephone_number_format_error_key))
    }

    "validate that a telephone number exceeding max length fails" in {
      val exceedMaxLengthEmail: String = "1" * 25
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> exceedMaxLengthEmail))

      formWithError.errors should contain(FormError(TelephoneNumberForm.telephoneNumberKey, telephone_number_length_error_key))
    }

    "validate that an empty field fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> ""))

      formWithError.errors should contain(FormError(TelephoneNumberForm.telephoneNumberKey, telephone_number_empty_error_key))
    }

  }

}
