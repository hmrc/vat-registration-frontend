
package pages.errors

import helpers.A11ySpec
import views.html.errors.maxConfirmationCodeAttemptsExceeded

class EmailConfirmationCodeMaxAttemptsExceededA11ySpec extends A11ySpec {

  val view: maxConfirmationCodeAttemptsExceeded = app.injector.instanceOf[maxConfirmationCodeAttemptsExceeded]

  "the maxConfirmationCodeAttemptsExceeded page" must {
    "pass all a11y checks" in {
      view("email", isTransactor = false).body must passAccessibilityChecks
    }
  }
}
