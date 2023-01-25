
package pages.applicant

import forms.RoleInTheBusinessForm
import helpers.A11ySpec
import models.api.{Trust, UkCompany}
import views.html.applicant.RoleInTheBusiness

class RoleInTheBusinessA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[RoleInTheBusiness]

  "the role in the business capture page" when {
    "the user is a trust" when {
      val form = RoleInTheBusinessForm(Trust, false)

      "the page is rendered without errors" must {
        "pass all accessibility tests" in {
          view(form, None, Trust).toString must passAccessibilityChecks
        }
      }

      "the page is rendered with errors" must {
        "pass all accessibility tests" in {
          view(form.bind(Map.empty[String, String]), None, partyType = Trust).toString must passAccessibilityChecks
        }
      }
    }
    "the user is not a trust" when {
      val form = RoleInTheBusinessForm(UkCompany, false)

      "the page is rendered without errors" must {
        "pass all accessibility tests" in {
          view(form, None, UkCompany).toString must passAccessibilityChecks
        }
      }

      "the page is rendered with errors" must {
        "pass all accessibility tests" in {
          view(form.bind(Map.empty[String, String]), None, partyType = UkCompany).toString must passAccessibilityChecks
        }
      }
    }
  }
}