
package pages.vatapplication

import forms.WarehouseNameForm
import helpers.A11ySpec
import views.html.vatapplication.WarehouseNameView

class WarehouseNameA11ySpec extends A11ySpec {

  val view: WarehouseNameView = app.injector.instanceOf[WarehouseNameView]

  "warehouse name page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNameForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNameForm.form.fill("warehouse name")).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(WarehouseNameForm.form.bind(Map("warehouseName" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}