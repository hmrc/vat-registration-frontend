
package pages.errors

import helpers.A11ySpec
import views.html.errors.MaxPasscodeAttemptsExceeded

class EmailPasscodesMaxAttemptsExceededA11ySpec extends A11ySpec {

  val view: MaxPasscodeAttemptsExceeded = app.injector.instanceOf[MaxPasscodeAttemptsExceeded]

  "the Max Passcode Attempts Exceeded page" must {
    "pass all a11y checks" in {
      view().body must passAccessibilityChecks
    }
  }
}
