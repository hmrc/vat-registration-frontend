
package pages.vatapplication

import forms.TaxRepForm
import helpers.A11ySpec
import views.html.vatapplication.TaxRepresentative

class TaxRepresentativeA11ySpec extends A11ySpec {

  val view: TaxRepresentative = app.injector.instanceOf[TaxRepresentative]

  "tax representative page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(TaxRepForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(TaxRepForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(TaxRepForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}