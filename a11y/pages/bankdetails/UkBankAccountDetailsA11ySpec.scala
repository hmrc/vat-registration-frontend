
package pages.bankdetails

import helpers.A11ySpec
import views.html.bankdetails.EnterCompanyBankAccountDetails
import forms.EnterCompanyBankAccountDetailsForm
import models.BankAccountDetails
import play.api.data.Form

class UkBankAccountDetailsA11ySpec extends A11ySpec {

  val view: EnterCompanyBankAccountDetails = app.injector.instanceOf[EnterCompanyBankAccountDetails]
  val form: Form[BankAccountDetails] = EnterCompanyBankAccountDetailsForm.form

  "the Enter Company Bank Account Details page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}