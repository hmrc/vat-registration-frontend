
package pages.vatapplication

import forms.DispatchFromWarehouseForm
import helpers.A11ySpec
import views.html.vatapplication.DispatchFromWarehouseView

class DispatchFromWarehouseA11ySpec extends A11ySpec {

  val view: DispatchFromWarehouseView = app.injector.instanceOf[DispatchFromWarehouseView]

  "dispatch from warehouse page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(DispatchFromWarehouseForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(DispatchFromWarehouseForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(DispatchFromWarehouseForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}