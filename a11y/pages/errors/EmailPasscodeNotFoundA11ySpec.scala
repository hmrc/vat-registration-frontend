
package pages.errors

import helpers.A11ySpec
import views.html.errors.PasscodeNotFound

class EmailPasscodeNotFoundA11ySpec extends A11ySpec {

  val view: PasscodeNotFound = app.injector.instanceOf[PasscodeNotFound]

  "the Passcode Not Found page" must {
    "pass all a11y checks" in {
      view("redirectUrl").body must passAccessibilityChecks
    }
  }
}
