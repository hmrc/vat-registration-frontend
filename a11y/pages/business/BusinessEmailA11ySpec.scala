

package pages.business

import forms.BusinessEmailAddressForm
import helpers.A11ySpec
import play.api.mvc.Call
import views.html.business.BusinessEmail

class BusinessEmailA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessEmail]
  val form = BusinessEmailAddressForm.form
  val action = Call("POST", controllers.business.routes.BusinessEmailController.submit.url)

  "the Business Email page" when {
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
