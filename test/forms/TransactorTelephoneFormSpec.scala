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

import play.api.data.{Form, FormError}
import testHelpers.VatRegSpec

class TransactorTelephoneFormSpec extends VatRegSpec {

  val telephoneNumberForm: Form[String] = TransactorTelephoneForm.form
  val testTelephoneNumber: String = "1234"
  val incorrectFormatErrorKey: String = "telephoneNumber.error.incorrectFormat"
  val nothingEnteredErrorKey: String = "telephoneNumber.error.nothingEntered"
  val incorrectLengthErrorKey: String = "telephoneNumber.error.incorrectLength"

  "The TransactorTelephoneForm" must {
    "validate that testTelephoneNumber is valid" in {
      val form = telephoneNumberForm.bind(Map(TransactorTelephoneForm.telephoneNumberKey -> testTelephoneNumber)).value

      form mustBe Some(testTelephoneNumber)
    }

    "validate that incorrect telephone number format fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TransactorTelephoneForm.telephoneNumberKey -> ":::><|"))

      formWithError.errors must contain(FormError(TransactorTelephoneForm.telephoneNumberKey, incorrectFormatErrorKey))
    }

    "validate that a telephone number exceeding max length fails" in {
      val exceedMaxLengthEmail: String = "1" * 25
      val formWithError = telephoneNumberForm.bind(Map(TransactorTelephoneForm.telephoneNumberKey -> exceedMaxLengthEmail))

      formWithError.errors must contain(FormError(TransactorTelephoneForm.telephoneNumberKey, incorrectLengthErrorKey))
    }

    "validate that an empty field fails" in {
      val formWithError = telephoneNumberForm.bind(Map(TransactorTelephoneForm.telephoneNumberKey -> ""))

      formWithError.errors must contain(FormError(TransactorTelephoneForm.telephoneNumberKey, nothingEnteredErrorKey))
    }

  }

}
