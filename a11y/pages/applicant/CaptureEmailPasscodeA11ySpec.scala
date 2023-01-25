
package pages.applicant

import forms.EmailPasscodeForm
import helpers.A11ySpec
import views.html.applicant.CaptureEmailPasscode

class CaptureEmailPasscodeA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[CaptureEmailPasscode]
  val form = EmailPasscodeForm.form

  "the email passcode page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests as non-transactor on normal flow" in {
        view("test-email", form, isTransactor = false, isNewPasscode = false).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as non-transactor on new passcode flow" in {
        view("test-email", form, isTransactor = false, isNewPasscode = true).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as transactor on normal flow" in {
        view("test-email", form, isTransactor = true, isNewPasscode = false).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as transactor on new passcode flow" in {
        view("test-email", form, isTransactor = true, isNewPasscode = true).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing email passcode value" must {
      "pass all accessibility tests as non-transactor on normal flow" in {
        view("test-email", form.bind(Map("email-passcode" -> "")), isTransactor = false, isNewPasscode = false).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as non-transactor on new passcode flow" in {
        view("test-email", form.bind(Map("email-passcode" -> "")), isTransactor = false, isNewPasscode = true).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as transactor on normal flow" in {
        view("test-email", form.bind(Map("email-passcode" -> "")), isTransactor = true, isNewPasscode = false).toString must passAccessibilityChecks
      }
      "pass all accessibility tests as transactor on new passcode flow" in {
        view("test-email", form.bind(Map("email-passcode" -> "")), isTransactor = true, isNewPasscode = true).toString must passAccessibilityChecks
      }
    }
  }
}
