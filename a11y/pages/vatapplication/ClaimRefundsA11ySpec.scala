
package pages.vatapplication

import forms.vatapplication.ChargeExpectancyForm
import helpers.A11ySpec
import views.html.vatapplication.ClaimRefunds

class ClaimRefundsA11ySpec extends A11ySpec {

  val view: ClaimRefunds = app.injector.instanceOf[ClaimRefunds]

  "claim refunds page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ChargeExpectancyForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ChargeExpectancyForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(ChargeExpectancyForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}