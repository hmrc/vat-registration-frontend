

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.{Vat2Required, Vat5LRequired}

class Vat5LRequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Vat5LRequired]

  "the Vat5L Required page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
