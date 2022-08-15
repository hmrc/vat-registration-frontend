

package pages.business

import forms.ScottishPartnershipNameForm
import helpers.A11ySpec
import views.html.business.ScottishPartnershipName

class ScottishPartnershipNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ScottishPartnershipName]

  "the ScottishPartnershipName page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(ScottishPartnershipNameForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(ScottishPartnershipNameForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
