
package pages.bankdetails

import forms.HasCompanyBankAccountForm
import helpers.A11ySpec
import views.html.bankdetails.HasCompanyBankAccountView

class HasBankAccountA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[HasCompanyBankAccountView]
  val form = HasCompanyBankAccountForm.form

  "the HasCompanyBankAccountView page" must {
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