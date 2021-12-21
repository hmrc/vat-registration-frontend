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

import forms.OverseasBankAccountForm._
import models.OverseasBankDetails
import org.scalatestplus.play.PlaySpec

class OverseasBankAccountFormSpec extends PlaySpec {

  "OverseasBankAccountForm" should {

    val form = OverseasBankAccountForm.form

    val validAccountName = "testAccountName"
    val validBic = "12345678"
    val validIban = "123456"

    "successfully bind data to the form with no errors and allow the return of a valid OverseasBankAccountDetails case class" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        BIC -> validBic,
        IBAN -> validIban
      )

      val validOverseasBankAccountDetails = OverseasBankDetails(validAccountName, validBic, validIban)

      val boundForm = form.bind(formData)
      boundForm.get mustBe validOverseasBankAccountDetails
    }

    "return a FormError when binding an empty account name to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> "",
        BIC -> validBic,
        IBAN -> validIban
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
        BIC -> validBic,
        IBAN -> validIban
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe ACCOUNT_NAME
      boundForm.errors.head.message mustBe accountNameInvalidKey
    }

    "return a FormError when binding an empty BIC to the form" in {
      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        BIC -> "",
        IBAN -> validIban
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe BIC
      boundForm.errors.head.message mustBe bicEmptyKey
    }

    "return a FormError when binding an invalid BIC to the form" in {
      val invalidBic = "ABCDE/"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        BIC -> invalidBic,
        IBAN -> validIban
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe BIC
      boundForm.errors.head.message mustBe bicInvalidKey
    }

    "return a FormError when binding an invalid IBAN to the form" in {
      val invalidIban = "ABCDEF/"

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        BIC -> validBic,
        IBAN -> invalidIban
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe IBAN
      boundForm.errors.head.message mustBe ibanInvalidKey
    }

    "return a single FormError when the IBAN is missing" in {
      val emptyIban = ""

      val formData = Map(
        ACCOUNT_NAME -> validAccountName,
        BIC -> validBic,
        IBAN -> emptyIban
      )

      val boundForm = form.bind(formData)

      boundForm.errors.size mustBe 1
      boundForm.errors.head.key mustBe IBAN
      boundForm.errors.head.message mustBe ibanEmptyKey
    }
  }
}
