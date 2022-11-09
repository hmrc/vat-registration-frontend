
package pages.bankdetails

import helpers.A11ySpec
import views.html.bankdetails.overseas_bank_account
import forms.OverseasBankAccountForm

class OverseasBankAccountA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[overseas_bank_account]
  val form = OverseasBankAccountForm.form

  "the overseas_bank_account page" when {
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