

package pages.business

import forms.BusinessNameForm
import helpers.A11ySpec
import views.html.business.BusinessName

class BusinessNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessName]

  "the Business Name page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(BusinessNameForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(BusinessNameForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
