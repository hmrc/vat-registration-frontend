
package pages

import forms.ApplicationReferenceForm
import helpers.A11ySpec
import views.html.ApplicationReference

class ApplicationReferenceA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ApplicationReference]
  val form = app.injector.instanceOf[ApplicationReferenceForm]

  "the Application Reference page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form()).toString must passAccessibilityChecks
      }
    }
  }

}
