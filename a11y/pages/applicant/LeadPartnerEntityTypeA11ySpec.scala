
package pages.applicant

import forms.PartnerForm
import helpers.A11ySpec
import views.html.applicant.PartnerEntityType

class LeadPartnerEntityTypeA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[PartnerEntityType]
  val form = PartnerForm.form("value")

  "the Partner entity type page" when {
    "the user is a transactor" when {
      "the page is rendered without errors" must {
        "pass all accessibility tests" in {
          view(form, isTransactor = true, 1).toString must passAccessibilityChecks
        }
      }

      "the page is rendered with errors" must {
        "pass all accessibility tests" in {
          view(form.bind(Map.empty[String, String]), isTransactor = true, 1).toString must passAccessibilityChecks
        }
      }
    }

    "the user is not a transactor" when {
      "the page is rendered without errors" must {
        "pass all accessibility tests" in {
          view(form, isTransactor = false, 1).toString must passAccessibilityChecks
        }
      }

      "the page is rendered with errors" must {
        "pass all accessibility tests" in {
          view(form.bind(Map.empty[String, String]), isTransactor = false, 1).toString must passAccessibilityChecks
        }
      }
    }
  }
}
