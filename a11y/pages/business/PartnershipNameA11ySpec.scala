

package pages.business

import forms.PartnershipNameForm
import helpers.A11ySpec
import views.html.business.PartnershipName

class PartnershipNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[PartnershipName]

  "the PartnershipName page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(PartnershipNameForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(PartnershipNameForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
