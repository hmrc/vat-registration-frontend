
package pages.bankdetails

import forms.NoUKBankAccountForm
import helpers.A11ySpec
import views.html.bankdetails.NoUkBankAccount

class NoUKBankAccountA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[NoUkBankAccount]
  val form = NoUKBankAccountForm.form

  "the NoUKBankAccount page" must {
    "pass all a11y checks" in {
      view(form).body must passAccessibilityChecks
    }
    "pass all a11y checks when there are form errors" in {
      view(form.bind(Map("" -> ""))).body must passAccessibilityChecks
    }
  }

}