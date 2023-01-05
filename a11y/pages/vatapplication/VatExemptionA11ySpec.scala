
package pages.vatapplication

import forms.VatExemptionForm
import helpers.A11ySpec
import views.html.vatapplication.VatExemption

class VatExemptionA11ySpec extends A11ySpec {

  val view: VatExemption = app.injector.instanceOf[VatExemption]

  "vat exemption page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(VatExemptionForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(VatExemptionForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(VatExemptionForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}