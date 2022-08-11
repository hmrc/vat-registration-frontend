
package pages.transactor

import forms.TransactorEmailAddressForm
import helpers.A11ySpec
import play.api.mvc.Call
import views.html.transactor.TransactorCaptureEmailAddress

class CaptureEmailAddressA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TransactorCaptureEmailAddress]
  val form = TransactorEmailAddressForm.form
  val action = Call("POST", controllers.transactor.routes.TransactorCaptureEmailPasscodeController.submit(false).url)

  "the CaptureEmailAddress page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(action, form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(action, form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}