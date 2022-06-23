
package pages.applicant

import forms.PreviousAddressForm
import helpers.A11ySpec
import views.html.applicant.previous_address

class PreviousAddressA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[previous_address]
  val form = PreviousAddressForm.form()

  "the previous address page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), None).toString must passAccessibilityChecks
      }
    }
  }
}