
package pages.transactor

import forms.AgentNameForm
import helpers.A11ySpec
import views.html.transactor.AgentNameView

class AgentNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[AgentNameView]
  val form = app.injector.instanceOf[AgentNameForm]

  "the agent name page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form()).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form().bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
