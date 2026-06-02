
package pages.bankdetails

import forms.CanYouProvideBankAccountDetailsForm
import helpers.A11ySpec
import play.api.data.Form
import views.html.bankdetails.CanYouProvideBankAccountDetailsView

class CanYouProvideBankAccountDetailsA11ySpec extends A11ySpec {

  val view: CanYouProvideBankAccountDetailsView = app.injector.instanceOf[CanYouProvideBankAccountDetailsView]
  val form: Form[Boolean]                       = CanYouProvideBankAccountDetailsForm.form

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
