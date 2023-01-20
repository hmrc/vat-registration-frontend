
package pages.partners

import forms.PartnerScottishPartnershipNameForm
import helpers.A11ySpec
import play.api.data.Form
import views.html.partners.PartnerScottishPartnershipName

class PartnerScottishPartnershipNameA11ySpec extends A11ySpec {
  val view: PartnerScottishPartnershipName = app.injector.instanceOf[PartnerScottishPartnershipName]
  val form: Form[String] = PartnerScottishPartnershipNameForm()

  "the scottish partnership name page" when {
    "no name given and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, 0).toString must passAccessibilityChecks
      }
    }

    "given a partnership name and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form.fill("partnerName"), 0).toString must passAccessibilityChecks
      }
    }

    "submitted with empty name and rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), 0).toString must passAccessibilityChecks
      }
    }
  }
}