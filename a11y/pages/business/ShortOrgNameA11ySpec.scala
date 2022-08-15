

package pages.business

import forms.ShortOrgNameForm
import helpers.A11ySpec
import views.html.business.ShortOrgName

class ShortOrgNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ShortOrgName]

  "the ShortOrgName page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(ShortOrgNameForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(ShortOrgNameForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
