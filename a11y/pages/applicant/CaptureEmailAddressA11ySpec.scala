
package pages.applicant

import controllers.applicant.routes
import forms.EmailAddressForm
import helpers.A11ySpec
import views.html.applicant.CaptureEmailAddress

class CaptureEmailAddressA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[CaptureEmailAddress]
  val form = EmailAddressForm.form
  val formAction = routes.CaptureEmailAddressController.submit

  "the email address page" when {
    "the page is rendered without errors when no applicant name given" must {
      "pass all accessibility tests" in {
        view(formAction, form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when an applicant name given" must {
      "pass all accessibility tests" in {
        view(formAction, form, Some("transactor-name")).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing email address value" must {
      "pass all accessibility tests" in {
        view(formAction, form.bind(Map("email-address" -> "")), None).toString must passAccessibilityChecks
      }
    }
  }
}