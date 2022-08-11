
package pages.transactor

import forms.PartOfOrganisationForm
import helpers.A11ySpec
import views.html.transactor.PartOfOrganisationView

class PartOfOrganisationA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[PartOfOrganisationView]

  "the Part of Organisation page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(PartOfOrganisationForm.form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(PartOfOrganisationForm.form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
