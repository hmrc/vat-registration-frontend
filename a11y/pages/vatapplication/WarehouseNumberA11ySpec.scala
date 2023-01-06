
package pages.vatapplication

import forms.WarehouseNumberForm
import helpers.A11ySpec
import views.html.vatapplication.WarehouseNumberView

class WarehouseNumberA11ySpec extends A11ySpec {

  val view: WarehouseNumberView = app.injector.instanceOf[WarehouseNumberView]

  "warehouse number page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNumberForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNumberForm.form.fill("warehouse number")).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNumberForm.form.bind(Map("warehouseNumber" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}