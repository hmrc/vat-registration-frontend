

package forms

import forms.OverseasBankAccountForm.{ACCOUNT_NAME, BIC, IBAN, accountNameEmptyKey, accountNameInvalidKey, bicEmptyKey, ibanEmptyKey}
import models.OverseasBankDetails
import org.scalatestplus.play.PlaySpec

class OverseasBankAccountFormSpec extends PlaySpec {

  "OverseasBankAccountForm" should {

    val form = OverseasBankAccountForm.form

    val validAccountName = "testAccountName"
    val validBic = "12345678"
    val validIban = "123456"

    "successfully bind data to the form with no errors and allow the return of a valid BankAccountDetails case class" in {
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
      boundForm.errors.head.message mustBe invalidBic
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
      boundForm.errors.head.message mustBe invalidIban
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
