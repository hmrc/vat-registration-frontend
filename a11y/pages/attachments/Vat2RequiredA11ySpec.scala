

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.Vat2Required

class Vat2RequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Vat2Required]

  "the Vat2 Required page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
