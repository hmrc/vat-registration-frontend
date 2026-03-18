
package pages.bankdetails

import forms.HasCompanyBankAccountForm
import helpers.A11ySpec
import play.api.data.Form
import views.html.bankdetails.CanProvideBankAccountView

class CanProvideBankAccountA11ySpec extends A11ySpec {

  val view: CanProvideBankAccountView = app.injector.instanceOf[CanProvideBankAccountView]
  val form: Form[Boolean] = HasCompanyBankAccountForm.form

  "the ProvideBankAccountView page" must {
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