

package pages.business

import forms.ContactPreferenceForm
import helpers.A11ySpec
import play.api.mvc.Call
import views.html.business.ContactPreferenceView

class ContactPreferenceA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ContactPreferenceView]
  val action = Call("POST", controllers.business.routes.ContactPreferenceController.submitContactPreference.url)

  "the ContactPreference page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(ContactPreferenceForm(), action).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(ContactPreferenceForm().bind(Map("" -> "")), action).body must passAccessibilityChecks
      }
    }
  }

}
