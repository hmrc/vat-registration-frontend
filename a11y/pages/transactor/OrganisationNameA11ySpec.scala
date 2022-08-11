
package pages.transactor

import forms.OrganisationNameForm
import helpers.A11ySpec
import views.html.transactor.OrganisationName

class OrganisationNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[OrganisationName]

  "the Organisation Name page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(OrganisationNameForm()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(OrganisationNameForm().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
