/*
 * Copyright 2024 HM Revenue & Customs
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

import models.BankAccountDetails
import org.scalatestplus.play.PlaySpec

class BankAccountDetailsFormSpec extends PlaySpec {

  val numStr             = 60
  val validAccountName   = s"${numStr}testAccountName"
  val validAccountNumber = "12345678"
  val validSortCode      = "123456"
  val validRollNumber    = "AB/121212"

  "EnterBankAccountDetailsForm (useBarsVerify OFF)" should {
    import forms.EnterCompanyBankAccountDetailsForm._

    val form = EnterCompanyBankAccountDetailsForm.form

    "successfully bind data to the form with no errors and allow the return of a valid BankAccountDetails case class" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val validBankAccountDetails = BankAccountDetails(validAccountName, validAccountNumber, validSortCode)

      val boundForm = form.bind(formData)
      boundForm.get mustBe validBankAccountDetails
    }

    "return a FormError when binding an empty account name to the form" in {
      val formData = Map(
        ACCOUNT_NAME   -> "",
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameEmptyKey
    }

    "return a FormError when binding an account name which exceeds max length to the form" in {
      val exceedMaxLength = "AlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimited"

      val formData = Map(
        ACCOUNT_NAME   -> exceedMaxLength,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameMaxLengthKey
    }

    "return a FormError when binding an invalid account name to the form" in {
      val invalidAccountName = "123#@~"

      val formData = Map(
        ACCOUNT_NAME   -> invalidAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameInvalidKey
    }

    "return a FormError when binding an empty account number to the form" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> "",
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberEmptyKey
    }

    "return a FormError when binding an invalid account number to the form" in {
      val invalidAccountNumber = "ABCDE"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return a FormError when binding an account number that is too short" in {
      val invalidAccountNumber = "12345"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return a FormError when binding an account number that is too long" in {
      val invalidAccountNumber = "123456789"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return No FormError when binding a valid account number with spaces to the form" in {
      val validAccountNumber = "123   456"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 0
    }

    "return a FormError when sort code is empty" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> ""
      )
      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeEmptyKey
    }

    "return a FormError when binding an invalid sort code part to the form" in {
      val invalidSortCode = "ABCDEF"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> invalidSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeInvalidKey
    }
  }

  "EnterBankAccountDetailsNewBarsForm (useBarsVerify ON)" should {
    import forms.EnterBankAccountDetailsForm._
    val form = EnterBankAccountDetailsForm.form

    "successfully bind valid data" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.get mustBe BankAccountDetails(validAccountName, validAccountNumber, validSortCode)
    }

    "return a FormError when account name is empty" in {
      val formData = Map(
        ACCOUNT_NAME   -> "",
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNameEmptyKey
    }

    "return a FormError when account name exceeds 60 characters" in {
      val exceedMaxLength = "AlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimited"

      val formData = Map(
        ACCOUNT_NAME   -> exceedMaxLength,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNameMaxLengthKey
    }

    "return a FormError when account name contains invalid characters" in {
      val invalidAccountName = "123#@~"

      val formData = Map(
        ACCOUNT_NAME   -> invalidAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNameInvalidKey
    }

    "return a FormError when account number is empty" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> "",
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNumberEmptyKey
    }

    "return a FormError when account number is fewer than 6 digits" in {
      val invalidAccountNumber = "12345"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNumberInvalidKey
    }

    "return a FormError when account number is 8 characters but contains letters" in {
      val invalidAccountNumber = "1234567A"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.accountNumberFormatKey
    }

    "return a FormError when sort code is empty" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> ""
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.sortCodeEmptyKey
    }

    "return a FormError when sort code is fewer than 6 digits" in {
      val invalidSortCode = "12345"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> invalidSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.sortCodeLengthKey
    }

    "return a FormError when sort code is 6 characters but contains letters" in {
      val invalidSortCode = "ABCDEF"

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> invalidSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.sortCodeFormatKey
    }

    "successfully bind with a valid roll number" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode,
        ROLL_NUMBER    -> validRollNumber
      )

      val boundForm = form.bind(formData)
      boundForm.errors mustBe empty
      boundForm.get.rollNumber mustBe Some(validRollNumber)
    }

    "successfully bind when roll number is not provided" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode
      )

      val boundForm = form.bind(formData)
      boundForm.errors mustBe empty
      boundForm.get.rollNumber mustBe None
    }

    "successfully bind when roll number is an empty string" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode,
        ROLL_NUMBER    -> ""
      )

      val boundForm = form.bind(formData)
      boundForm.errors mustBe empty
      boundForm.get.rollNumber mustBe None
    }

    "return a FormError when roll number exceeds 25 characters" in {
      val longRollNumber = "A" * 26

      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode,
        ROLL_NUMBER    -> longRollNumber
      )

      val boundForm = form.bind(formData)
      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ROLL_NUMBER
      boundForm.errors.head.message mustBe EnterBankAccountDetailsForm.rollNumberInvalidKey
    }

    "successfully bind and strip spaces from roll number before storing" in {
      val formData = Map(
        ACCOUNT_NAME   -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE      -> validSortCode,
        ROLL_NUMBER    -> "AB 121 212"
      )

      val boundForm = form.bind(formData)
      boundForm.errors mustBe empty
      boundForm.get.rollNumber mustBe Some("AB121212")
    }
  }
}
