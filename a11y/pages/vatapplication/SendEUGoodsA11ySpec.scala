
package pages.vatapplication

import forms.SendEuGoodsForm
import helpers.A11ySpec
import views.html.vatapplication.SendEUGoodsView

class SendEUGoodsA11ySpec extends A11ySpec {

  val view: SendEUGoodsView = app.injector.instanceOf[SendEUGoodsView]

  "send EU goods page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SendEuGoodsForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SendEuGoodsForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(SendEuGoodsForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}