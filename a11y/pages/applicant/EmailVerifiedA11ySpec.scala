
package pages.applicant

import controllers.applicant.routes
import helpers.A11ySpec
import views.html.applicant.EmailVerified

class EmailVerifiedA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[EmailVerified]
  val formAction = routes.EmailAddressVerifiedController.submit

  "the email address verified page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(formAction).toString must passAccessibilityChecks
      }
    }
  }
}