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
import play.api.data.{Form, FormError}
import testHelpers.VatRegSpec

class TelephoneNumberFormSpec extends VatRegSpec {

  val telephoneNumberForm: Form[String] = TelephoneNumberForm.form
  val testTelephoneNumber: String = "0123456789"
  //LDS ignore - adding exemption rule to ignore line 28 as it is not secret
  val testTelephoneNumberWithAllowedCharacters: String = "+1234 4589 7987"
  val testTelephoneNumberWithMultiplePluses = "++1234 4589 7987"
  val invalid_telephone_number_format_error_key: String = "capture-telephone-number.error.invalid"
  val telephone_number_empty_error_key: String = "capture-telephone-number.error.missing"
  val telephone_number_min_length_error_key: String = "capture-telephone-number.error.minlength"
  val telephone_number_max_length_error_key: String = "capture-telephone-number.error.maxlength"

  "The TelephoneNumberForm" must {

    "validate that telephone number is valid" in {
      val form = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> testTelephoneNumber)).value

      form mustBe Some(testTelephoneNumber)
    }

    "validate that telephone number start with + and spaces is valid" in {
      val expectedPhoneNumber = testTelephoneNumberWithAllowedCharacters.replaceAll(" ", "")
      //LDS ignore - adding exemption rule to ignore line 46 as it is not secret
      val form = Map(TelephoneNumberForm.telephoneNumberKey -> testTelephoneNumberWithAllowedCharacters)

      telephoneNumberForm.bind(form) shouldContainValue expectedPhoneNumber
    }

    "validate that telephone number containing multiple + signs fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> testTelephoneNumberWithMultiplePluses))

      formWithError.errors must contain(FormError(TelephoneNumberForm.telephoneNumberKey, invalid_telephone_number_format_error_key))
    }

    "validate that incorrect telephone number format fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> "invalid telephone number"))

      formWithError.errors must contain(FormError(TelephoneNumberForm.telephoneNumberKey, invalid_telephone_number_format_error_key))
    }

    "validate that telephone number exceeding min length fails" in {
      val exceedMinLengthPhoneNumber: String = "01234"
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> exceedMinLengthPhoneNumber))

      formWithError.errors must contain(FormError(TelephoneNumberForm.telephoneNumberKey, telephone_number_min_length_error_key))
    }

    "validate that telephone number exceeding max length fails" in {
      val exceedMaxLengthPhoneNumber: String = "01234567890123456789"
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> exceedMaxLengthPhoneNumber))

      formWithError.errors must contain(FormError(TelephoneNumberForm.telephoneNumberKey, telephone_number_max_length_error_key))
    }

    "validate that an empty field fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TelephoneNumberForm.telephoneNumberKey -> ""))

      formWithError.errors must contain(FormError(TelephoneNumberForm.telephoneNumberKey, telephone_number_empty_error_key))
    }

  }

}
