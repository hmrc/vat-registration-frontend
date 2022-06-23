
package pages.applicant

import forms.FormerNameCaptureForm
import helpers.A11ySpec
import views.html.applicant.FormerNameCapture

class FormerNameCaptureA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[FormerNameCapture]
  val form = FormerNameCaptureForm.form

  "the former name capture page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), None).toString must passAccessibilityChecks
      }
    }
  }
}