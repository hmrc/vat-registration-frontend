
package pages.vatapplication

import forms.ApplyForEoriForm
import helpers.A11ySpec
import views.html.vatapplication.ApplyForEori

class ApplyForEoriA11ySpec extends A11ySpec {

  val view: ApplyForEori = app.injector.instanceOf[ApplyForEori]

  "apply for eori page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ApplyForEoriForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ApplyForEoriForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(ApplyForEoriForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
