
package pages.vatapplication

import forms.SendGoodsOverseasForm
import helpers.A11ySpec
import views.html.vatapplication.SendGoodsOverseasView

class SendGoodsOverseasA11ySpec extends A11ySpec {

  val view: SendGoodsOverseasView = app.injector.instanceOf[SendGoodsOverseasView]

  "send goods overseas page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SendGoodsOverseasForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SendGoodsOverseasForm.form.fill(true)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(SendGoodsOverseasForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}