

package pages.attachments

import forms.AttachmentMethodForm
import helpers.A11ySpec
import views.html.attachments.ChooseAttachmentMethod

class ChooseAttachmentMethodA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ChooseAttachmentMethod]
  val form = app.injector.instanceOf[AttachmentMethodForm]

  "the Choose Attachment Method page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form()).toString must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form().bind(Map.empty[String, String])).toString must passAccessibilityChecks
      }
    }
  }

}
