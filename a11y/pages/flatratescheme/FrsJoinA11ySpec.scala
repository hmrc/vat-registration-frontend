
package pages.flatratescheme

import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import helpers.A11ySpec
import views.html.flatratescheme.JoinFrs

class FrsJoinA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[JoinFrs]
  val form = YesOrNoFormFactory.form()("frs.join")

  "the frs join page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill(YesOrNoAnswer(true))).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}