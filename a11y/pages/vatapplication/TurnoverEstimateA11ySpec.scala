
package pages.vatapplication

import forms.TurnoverEstimateForm
import helpers.A11ySpec
import views.html.vatapplication.TurnoverEstimate

class TurnoverEstimateA11ySpec extends A11ySpec {

  val view: TurnoverEstimate = app.injector.instanceOf[TurnoverEstimate]

  "turnover estimate page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(TurnoverEstimateForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(TurnoverEstimateForm.form.fill(100)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(TurnoverEstimateForm.form.bind(Map("turnoverEstimate" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}