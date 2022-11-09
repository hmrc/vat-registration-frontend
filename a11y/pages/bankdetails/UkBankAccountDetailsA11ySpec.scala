
package pages.bankdetails

import helpers.A11ySpec
import views.html.bankdetails.enter_company_bank_account_details
import forms.EnterBankAccountDetailsForm

class UkBankAccountDetailsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[enter_company_bank_account_details]
  val form = EnterBankAccountDetailsForm.form

  "the enter_company_bank_account_details page" when {
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