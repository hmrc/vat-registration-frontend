
package pages.errors

import helpers.A11ySpec
import views.html.errors.MaxConfirmationCodeAttemptsExceeded

class EmailConfirmationCodeMaxAttemptsExceededA11ySpec extends A11ySpec {

  val view: MaxConfirmationCodeAttemptsExceeded = app.injector.instanceOf[MaxConfirmationCodeAttemptsExceeded]

  "the Max Confirmation Code Attempts Exceeded page" must {
    "pass all a11y checks" in {
      view("email", isTransactor = false).body must passAccessibilityChecks
    }
  }
}
