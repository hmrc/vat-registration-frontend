
package pages.bankdetails

import helpers.A11ySpec
import views.html.bankdetails.EnterCompanyBankAccountDetails
import forms.EnterBankAccountDetailsForm

class UkBankAccountDetailsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[EnterCompanyBankAccountDetails]
  val form = EnterBankAccountDetailsForm.form

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