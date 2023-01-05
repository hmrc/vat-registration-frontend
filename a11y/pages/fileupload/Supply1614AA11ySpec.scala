

package pages.fileupload

import forms.Supply1614AForm
import helpers.A11ySpec
import views.html.fileupload.Supply1614A

class Supply1614AA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Supply1614A]
  val form = Supply1614AForm.form

  "the Supply1614A page" when {
    "the page is rendered with no errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
  }

}
