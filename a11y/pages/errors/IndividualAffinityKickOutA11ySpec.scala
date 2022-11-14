
package pages.errors

import helpers.A11ySpec
import views.html.errors.IndividualAffinityKickOut

class IndividualAffinityKickOutA11ySpec extends A11ySpec {

  val view: IndividualAffinityKickOut = app.injector.instanceOf[IndividualAffinityKickOut]

  "the IndividualAffinityKickOut page" must {
    "pass all a11y checks" in {
      view().body must passAccessibilityChecks
    }
  }
}
