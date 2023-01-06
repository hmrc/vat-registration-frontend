
package pages.vatapplication

import helpers.A11ySpec
import views.html.vatapplication.MandatoryStartDateNoChoiceView

class MandatoryStartDateNoChoiceA11ySpec extends A11ySpec {

  val view: MandatoryStartDateNoChoiceView = app.injector.instanceOf[MandatoryStartDateNoChoiceView]

  "mandatory start date no choice page" when {
    "there are no rendering errors" must {
      "pass all a11y checks" in {
        view("example date").body must passAccessibilityChecks
      }
    }
  }
}