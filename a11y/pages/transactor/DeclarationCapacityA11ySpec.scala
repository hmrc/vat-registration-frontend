
package pages.transactor

import forms.DeclarationCapacityForm
import helpers.A11ySpec
import views.html.transactor.DeclarationCapacityView

class DeclarationCapacityA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[DeclarationCapacityView]

  "the Declaration Capacity page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(DeclarationCapacityForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(DeclarationCapacityForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
