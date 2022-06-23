
package pages.applicant

import controllers.applicant.routes
import forms.EmailPasscodeForm
import helpers.A11ySpec
import views.html.applicant.capture_email_passcode

class CaptureEmailPasscodeA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[capture_email_passcode]
  val form = EmailPasscodeForm.form
  val formAction = routes.CaptureEmailPasscodeController.submit

  "the email passcode page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view("test-email", formAction, form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing email passcode value" must {
      "pass all accessibility tests" in {
        view("test-email", formAction, form.bind(Map("email-passcode" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}
