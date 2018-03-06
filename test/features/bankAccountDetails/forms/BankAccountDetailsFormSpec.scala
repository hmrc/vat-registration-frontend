/*
 * Copyright 2018 HM Revenue & Customs
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

package features.bankAccountDetails.forms

import features.bankAccountDetails.models.BankAccountDetails
import EnterBankAccountDetailsForm._
import org.scalatestplus.play.PlaySpec

class BankAccountDetailsFormSpec extends PlaySpec {

  "EnterBankAccountDetailsForm" should {

    val form = EnterBankAccountDetailsForm.form

    val validAccountName = "testAccountName"
    val validAccountNumber = "12345678"
    val (part1, part2, part3) = ("12", "34", "56")
    val validSortCode = s"$part1-$part2-$part3"

    "successfully bind data to the form with no errors and allow the return of a valid BankAccountDetails case class" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> part1,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
      )

      val validBankAccountDetails = BankAccountDetails(validAccountName, validSortCode, validAccountNumber)

      val boundForm = form.bind(formData)
      boundForm.get mustBe validBankAccountDetails
    }

    "return a FormError when binding an empty account name to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> "",
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> part1,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameEmptyKey
    }

    "return a FormError when binding an invalid account name to the form" in {
      val invalidAccountName = "123#@~"

      val formData = Map(
        ACCOUNT_NAME -> invalidAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> part1,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
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
        "sortCode.part1" -> part1,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
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
        "sortCode.part1" -> part1,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NUMBER
      boundForm.errors.head.message mustBe accountNumberInvalidKey
    }

    "return a FormError when binding an empty sic code part to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> "",
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeEmptyKey
    }

    "return a FormError when binding an invalid sic code part to the form" in {
      val invalidSicCodePart = "ABCD"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> invalidSicCodePart,
        "sortCode.part2" -> part2,
        "sortCode.part3" -> part3
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeInvalidKey
    }

    "return a single FormError when all three sort code parts are missing" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        ACCOUNT_NUMBER -> validAccountNumber,
        "sortCode.part1" -> "",
        "sortCode.part2" -> "",
        "sortCode.part3" -> ""
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe SORT_CODE
      boundForm.errors.head.message mustBe sortCodeEmptyKey
    }
  }
}
