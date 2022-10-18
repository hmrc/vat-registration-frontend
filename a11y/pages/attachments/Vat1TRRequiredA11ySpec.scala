

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.Vat1TRRequired

class Vat1TRRequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Vat1TRRequired]

  "the Vat1TR Required page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
