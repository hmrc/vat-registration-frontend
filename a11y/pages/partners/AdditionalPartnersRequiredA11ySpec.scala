
package pages.partners

import helpers.A11ySpec
import views.html.partners.AdditionalPartnersRequired

class AdditionalPartnersRequiredA11ySpec extends A11ySpec {
  val view: AdditionalPartnersRequired = app.injector.instanceOf[AdditionalPartnersRequired]

  "Additional partners required page" when {
    "rendered without errors" must {
      "pass all accessibility tests" in {
        view().toString must passAccessibilityChecks
      }
    }
  }
}