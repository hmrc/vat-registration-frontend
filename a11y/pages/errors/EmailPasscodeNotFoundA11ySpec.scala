
package pages.errors

import helpers.A11ySpec
import views.html.errors.passcode_not_found

class EmailPasscodeNotFoundA11ySpec extends A11ySpec {

  val view: passcode_not_found = app.injector.instanceOf[passcode_not_found]

  "the passcode_not_found page" must {
    "pass all a11y checks" in {
      view("redirectUrl").body must passAccessibilityChecks
    }
  }
}
