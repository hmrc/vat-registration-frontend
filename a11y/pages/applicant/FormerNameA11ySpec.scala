
package pages.applicant

import forms.FormerNameForm
import helpers.A11ySpec
import views.html.applicant.FormerName

class FormerNameA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[FormerName]
  val form = FormerNameForm.form

  "the telephone number page" when {
    "the page is rendered without errors when no name given" must {
      "pass all accessibility tests" in {
        view(form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a name given" must {
      "pass all accessibility tests" in {
        view(form, Some("name")).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), None).toString must passAccessibilityChecks
      }
    }
  }
}