

package pages.attachments

import forms.TaxRepForm
import helpers.A11ySpec
import views.html.vatapplication.TaxRepresentative

class TaxRepresentativeA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TaxRepresentative]
  val form = TaxRepForm.form

  "the Tax Representative page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form).toString must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map.empty[String, String])).toString must passAccessibilityChecks
      }
    }
  }

}
