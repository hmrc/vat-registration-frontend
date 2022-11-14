
package pages.errors

import helpers.A11ySpec
import views.html.errors.maxPasscodeAttemptsExceeded

class EmailPasscodesMaxAttemptsExceededA11ySpec extends A11ySpec {

  val view: maxPasscodeAttemptsExceeded = app.injector.instanceOf[maxPasscodeAttemptsExceeded]

  "the passcode_not_found page" must {
    "pass all a11y checks" in {
      view().body must passAccessibilityChecks
    }
  }
}
