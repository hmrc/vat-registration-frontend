

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.IdentityEvidenceRequired

class IdentityEvidenceRequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[IdentityEvidenceRequired]

  "the Identity Evidence Required page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
