
package pages.vatapplication

import forms.ZeroRatedSuppliesForm
import helpers.A11ySpec
import play.api.mvc.Call
import views.html.vatapplication.zero_rated_supplies

class ZeroRatedSuppliesA11ySpec extends A11ySpec {

  val testCall: Call = Call("POST", "/test-url")
  val view: zero_rated_supplies = app.injector.instanceOf[zero_rated_supplies]

  "zero rated supplies page" when {
    val form = ZeroRatedSuppliesForm.form(15000)

    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(testCall, form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(testCall, form.fill(100)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(testCall, form.bind(Map("zeroRatedSupplies" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}