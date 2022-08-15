

package pages.business

import forms.BusinessTelephoneNumberForm
import helpers.A11ySpec
import views.html.business.BusinessTelephoneNumber

class BusinessTelephoneNumberA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessTelephoneNumber]
  val form = BusinessTelephoneNumberForm.form

  "the BusinessTelephoneNumber page" when {
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
