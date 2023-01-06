
package pages.vatapplication

import forms.ReceiveGoodsNipForm
import helpers.A11ySpec
import views.html.vatapplication.ReceiveGoodsNip

class ReceiveGoodsNipA11ySpec extends A11ySpec {

  val view: ReceiveGoodsNip = app.injector.instanceOf[ReceiveGoodsNip]

  "receive goods nip page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ReceiveGoodsNipForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(ReceiveGoodsNipForm.form.fill((true, Some(100)))).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(ReceiveGoodsNipForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }

    "there are form errors when incomplete values submitted" must {
      "pass all a11y checks" in {
        view(ReceiveGoodsNipForm.form.bind(Map("value" -> "true"))).body must passAccessibilityChecks
      }
    }
  }
}