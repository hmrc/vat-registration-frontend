
package pages.partners

import forms.partners.RemovePartnerEntityForm
import helpers.A11ySpec
import views.html.partners.RemovePartnerEntity

class RemovePartnerEntityA11ySpec extends A11ySpec {
  val partnerName: Option[String] = Some("partner-name")
  val view: RemovePartnerEntity = app.injector.instanceOf[RemovePartnerEntity]

  "remove partner entity page" when {
    "rendered with no form errors" must {
      "pass all a11y checks" in {
        view(RemovePartnerEntityForm(partnerName).form, partnerName, 0).body must passAccessibilityChecks
      }
    }

    "submitted with a selection and rendered without errors" must {
      "pass all a11y checks" in {
        view(RemovePartnerEntityForm(partnerName).form.bind(Map("value" -> "true")), partnerName, 0).body must passAccessibilityChecks
      }
    }

    "submitted with no selection and rendered with errors" must {
      "pass all a11y checks" in {
        view(RemovePartnerEntityForm(partnerName).form.bind(Map("value" -> "")), partnerName, 0).body must passAccessibilityChecks
      }
    }
  }
}
