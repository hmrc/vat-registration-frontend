
package pages.applicant

import forms.LeadPartnerForm
import helpers.A11ySpec
import views.html.applicant.lead_partner_entity_type

class LeadPartnerEntityTypeA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[lead_partner_entity_type]
  val form = LeadPartnerForm.form

  "the lead partner entity type page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}