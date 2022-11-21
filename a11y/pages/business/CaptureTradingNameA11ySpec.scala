

package pages.business

import forms.CaptureTradingNameForm
import helpers.A11ySpec
import views.html.business.CaptureTradingNameView

class CaptureTradingNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[CaptureTradingNameView]
  val form = CaptureTradingNameForm.form

  "the SoleTraderName page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
