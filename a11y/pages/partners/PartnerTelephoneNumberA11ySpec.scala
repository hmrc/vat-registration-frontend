
package pages.partners

import forms.PartnerTelephoneForm
import helpers.A11ySpec
import play.api.data.Form
import views.html.partners.PartnerTelephoneNumber

class PartnerTelephoneNumberA11ySpec extends A11ySpec {
  val view: PartnerTelephoneNumber = app.injector.instanceOf[PartnerTelephoneNumber]
  val form: Form[String] = PartnerTelephoneForm.form

  "the partner telephone number page" when {
    "no telephone number given and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, 0, None).toString must passAccessibilityChecks
      }
    }

    "telephone number given and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form.fill("012345667"), 0, None).toString must passAccessibilityChecks
      }
    }

    "submitted with no telephone number and rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map(PartnerTelephoneForm.partnerTelephoneKey -> "")), 0, None).toString must passAccessibilityChecks
      }
    }
  }
}
