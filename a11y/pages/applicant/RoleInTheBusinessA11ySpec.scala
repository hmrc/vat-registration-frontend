
package pages.applicant

import forms.RoleInTheBusinessForm
import helpers.A11ySpec
import views.html.applicant.role_in_the_business

class RoleInTheBusinessA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[role_in_the_business]
  val form = RoleInTheBusinessForm()

  "the role in the business capture page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), None).toString must passAccessibilityChecks
      }
    }
  }
}