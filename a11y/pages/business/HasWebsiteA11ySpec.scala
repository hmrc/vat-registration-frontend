

package pages.business

import forms.HasWebsiteForm
import helpers.A11ySpec
import views.html.business.HasWebsite

class HasWebsiteA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[HasWebsite]
  val form = HasWebsiteForm.form

  "the HasWebsite page" when {
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
