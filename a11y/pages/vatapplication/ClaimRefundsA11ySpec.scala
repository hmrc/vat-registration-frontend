
package pages.vatapplication

import forms.ApplyForEoriForm
import forms.vatapplication.ChargeExpectancyForm
import helpers.A11ySpec
import views.html.vatapplication.{apply_for_eori, claim_refunds_view}

class ClaimRefundsA11ySpec extends A11ySpec {

  val view: claim_refunds_view = app.injector.instanceOf[claim_refunds_view]

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