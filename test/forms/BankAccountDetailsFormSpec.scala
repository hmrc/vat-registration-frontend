/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.EnterBankAccountDetailsForm._
import models.BankAccountDetails
import org.scalatestplus.play.PlaySpec

class BankAccountDetailsFormSpec extends PlaySpec {

  "EnterBankAccountDetailsForm" should {

    val form = EnterBankAccountDetailsForm.form

    val validAccountName = 60 + "testAccountName"
    val validAccountNumber = "12345678"
    val validSortCode = "123456"

    "successfully bind data to the form with no errors and allow the return of a valid BankAccountDetails case class" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> validSortCode
      )

      val validBankAccountDetails = BankAccountDetails(validAccountName, validAccountNumber, validSortCode)

      val boundForm = form.bind(formData)
      boundForm.get mustBe validBankAccountDetails
    }

    "return a FormError when binding an empty account name to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> "",
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameEmptyKey
    }

    "return a FormError when binding an account name which exceeds max length to the form" in {
      val exceedMaxLength = "AlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimitedAlPacinoLimited"

      val formData = Map(
        ACCOUNT_NAME -> exceedMaxLength,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameMaxLengthKey
    }

    "return a FormError when binding an invalid account name to the form" in {
      val invalidAccountName = "123#@~"

      val formData = Map(
        ACCOUNT_NAME -> invalidAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameInvalidKey
    }

    "return a FormError when binding an empty account number to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> "",
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberEmptyKey
    }

    "return a FormError when binding an invalid account number to the form" in {
      val invalidAccountNumber = "ABCDE"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return a FormError when binding an account number that is too short" in {
      val invalidAccountNumber = "12345"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return a FormError when binding an account number that is too long" in {
      val invalidAccountNumber = "123456789"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> invalidAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return No FormError when binding a valid account number with spaces to the form" in {
      val validAccountNumber = "123   456"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> validSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 0
    }


    "return a FormError when binding an invalid sort code part to the form" in {
      val invalidSortCode = "ABCDEF"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> invalidSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeInvalidKey
    }
    "return No FormError when binding a sort code with spaces ( ) part to the form" in {
      val invalidSortCode = "02 03  06"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> invalidSortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 0
    }

    "return a single FormError when the sort code is missing" in {
      val emptySortCode = ""

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        SORT_CODE -> emptySortCode
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeEmptyKey
    }
  }
}
