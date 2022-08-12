

package pages.business

import forms.SoleTraderNameForm
import helpers.A11ySpec
import views.html.business.soletrader_name

class SoleTraderNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[soletrader_name]
  val form = SoleTraderNameForm.form

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
