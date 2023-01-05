

package pages.fileupload

import forms.Supply1614HForm
import helpers.A11ySpec
import views.html.fileupload.Supply1614H

class Supply1614HA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Supply1614H]
  val form = Supply1614HForm.form

  "the Supply1614H page" when {
    "the page is rendered with no errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
  }

}