
package pages.applicant

import controllers.applicant.routes
import forms.TelephoneNumberForm
import helpers.A11ySpec
import views.html.applicant.CaptureTelephoneNumber

class CaptureTelephoneNumberA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[CaptureTelephoneNumber]
  val form = TelephoneNumberForm.form
  val formAction = routes.CaptureTelephoneNumberController.submit

  "the telephone number page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(formAction, form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing email passcode value" must {
      "pass all accessibility tests" in {
        view(formAction, form.bind(Map("telephone-number" -> "")), None).toString must passAccessibilityChecks
      }
    }
  }
}