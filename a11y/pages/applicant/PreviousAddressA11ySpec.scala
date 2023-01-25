
package pages.applicant

import forms.PreviousAddressForm
import helpers.A11ySpec
import models.api.{Address, Country}
import views.html.applicant.PreviousAddress

class PreviousAddressA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[PreviousAddress]
  val form = PreviousAddressForm.form()
  val address = Address("line1", Some("line2"), Some("line3"), Some("line4"), Some("line5"), Some("postCode"), Some(Country(Some("UK"), Some("United Kingdom"))))

  "the previous address page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, None, address).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), None, address).toString must passAccessibilityChecks
      }
    }
  }
}