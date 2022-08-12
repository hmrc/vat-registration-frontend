

package pages.business

import forms.BusinessWebsiteAddressForm
import helpers.A11ySpec
import play.api.mvc.Call
import views.html.business.BusinessWebsiteAddress

class BusinessWebsiteAddressA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessWebsiteAddress]
  val form = BusinessWebsiteAddressForm.form
  val action = Call("POST", controllers.business.routes.BusinessWebsiteAddressController.submit.url)

  "the BusinessWebsiteAddress page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(action, form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(action, form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
