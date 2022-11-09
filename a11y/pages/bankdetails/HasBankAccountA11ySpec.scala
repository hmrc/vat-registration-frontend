
package pages.bankdetails

import forms.HasCompanyBankAccountForm
import helpers.A11ySpec
import views.html.bankdetails.has_company_bank_account

class HasBankAccountA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[has_company_bank_account]
  val form = HasCompanyBankAccountForm.form

  "the has_company_bank_account page" must {
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