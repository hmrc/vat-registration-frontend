
package pages.transactor

import forms.TelephoneNumberForm
import helpers.A11ySpec
import views.html.transactor.TelephoneNumber

class TelephoneNumberA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TelephoneNumber]
  val form = TelephoneNumberForm.form

  "The Telephone Number page" when {
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
