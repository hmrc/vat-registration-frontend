
package pages.vatapplication

import forms.vatapplication.ReturnsFrequencyForm
import helpers.A11ySpec
import models.api.vatapplication.Monthly
import views.html.vatapplication.return_frequency_view

class ReturnFrequencyA11ySpec extends A11ySpec {

  val view: return_frequency_view = app.injector.instanceOf[return_frequency_view]

  "return frequency page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ReturnsFrequencyForm.form, showAAS = true, showMonthly = true).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ReturnsFrequencyForm.form.fill(Monthly), showAAS = true, showMonthly = true).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(
          ReturnsFrequencyForm.form.bind(Map("value" -> "")),
          showAAS = true,
          showMonthly = true
        ).body must passAccessibilityChecks
      }
    }
  }
}