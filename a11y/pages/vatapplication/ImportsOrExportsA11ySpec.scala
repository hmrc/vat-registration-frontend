
package pages.vatapplication

import forms.ImportsOrExportsForm
import helpers.A11ySpec
import views.html.vatapplication.ImportsOrExports

class ImportsOrExportsA11ySpec extends A11ySpec {

  val view: ImportsOrExports = app.injector.instanceOf[ImportsOrExports]

  "imports or exports page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ImportsOrExportsForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ImportsOrExportsForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(ImportsOrExportsForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}