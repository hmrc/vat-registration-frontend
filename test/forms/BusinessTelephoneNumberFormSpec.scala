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
import play.api.data.FormError
import testHelpers.VatRegSpec

class BusinessTelephoneNumberFormSpec extends VatRegSpec {

  val testForm = BusinessTelephoneNumberForm.form
  val telephone_empty_error_key = "validation.business.telephoneNumber.missing"
  val telephone_invalid_error_key = "validation.business.telephoneNumber.invalid"
  val telephone_min_length_error_key = "validation.business.telephoneNumber.minlength"
  val telephone_max_length_error_key = "validation.business.telephoneNumber.maxlength"

  "A business telephone number form" must {

    val DAYTIME_PHONE = "0123456789"

    "validate that phone number is valid" in {
      val data = Map(BusinessTelephoneNumberForm.telephoneNumberKey -> Seq(DAYTIME_PHONE))
      testForm.bindFromRequest(data) shouldContainValue DAYTIME_PHONE
    }

    "validate that phone number start with + and spaces is valid" in {
      val phoneWithAllowedCharacters = "+1234 4589 7987"
      val expectedPhoneNumber = phoneWithAllowedCharacters.replaceAll(" ", "")
      val data = Map(BusinessTelephoneNumberForm.telephoneNumberKey -> phoneWithAllowedCharacters)
      testForm.bind(data) shouldContainValue expectedPhoneNumber
    }

    "validate that phone number containing multiple + signs fails" in {
      val phoneWithMultiplePluses = "++1234 4589 7987"
      val formWithError = testForm.bind(Map(BusinessTelephoneNumberForm.telephoneNumberKey -> phoneWithMultiplePluses))
      formWithError.errors must contain(FormError(BusinessTelephoneNumberForm.telephoneNumberKey, telephone_invalid_error_key))
    }

    "validate that incorrect phone format fails" in {
      val formWithError = testForm.bind(Map(BusinessTelephoneNumberForm.telephoneNumberKey -> "invalid phone"))

      formWithError.errors must contain(FormError(BusinessTelephoneNumberForm.telephoneNumberKey, telephone_invalid_error_key))
    }

    "validate that phone number exceeding min length fails" in {
      val exceedMinLengthPhoneNumber: String = "01234"
      val formWithError = testForm.bind(Map(BusinessTelephoneNumberForm.telephoneNumberKey -> exceedMinLengthPhoneNumber))

      formWithError.errors must contain(FormError(BusinessTelephoneNumberForm.telephoneNumberKey, telephone_min_length_error_key))
    }

    "validate that phone number exceeding max length fails" in {
      val exceedMaxLengthPhoneNumber: String = "01234567890123456789"
      val formWithError = testForm.bind(Map(BusinessTelephoneNumberForm.telephoneNumberKey -> exceedMaxLengthPhoneNumber))

      formWithError.errors must contain(FormError(BusinessTelephoneNumberForm.telephoneNumberKey, telephone_max_length_error_key))
    }

    "validate that an empty field fails" in {
      val formWithError = testForm.bind(Map(BusinessTelephoneNumberForm.telephoneNumberKey -> ""))

      formWithError.errors must contain(FormError(BusinessTelephoneNumberForm.telephoneNumberKey, telephone_empty_error_key))
    }

  }
}
