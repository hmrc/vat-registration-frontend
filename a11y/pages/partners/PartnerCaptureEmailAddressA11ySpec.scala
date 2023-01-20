
package pages.partners

import helpers.A11ySpec
import play.api.data.Form
import forms.PartnerEmailAddressForm
import views.html.partners.PartnerCaptureEmailAddress

class PartnerCaptureEmailAddressA11ySpec extends A11ySpec {
  val view: PartnerCaptureEmailAddress = app.injector.instanceOf[PartnerCaptureEmailAddress]
  val form: Form[String] = PartnerEmailAddressForm.form

  "the partner email address page" when {
    "no email address given and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, 0, None).toString must passAccessibilityChecks
      }
    }

    "email address given and rendered without errors" must {
      "pass all accessibility tests" in {
        view(form.fill("partner-email"), 0, None).toString must passAccessibilityChecks
      }
    }

    "submitted with no email address and rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map(PartnerEmailAddressForm.emailKey -> "")), 0, None).toString must passAccessibilityChecks
      }
    }
  }
}
