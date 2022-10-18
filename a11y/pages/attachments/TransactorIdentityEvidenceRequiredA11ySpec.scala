

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.TransactorIdentityEvidenceRequired

class TransactorIdentityEvidenceRequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TransactorIdentityEvidenceRequired]

  "the Transactor Identity Evidence Required page" when {
    "the page is rendered without errors for transactor" must {
      "pass all a11y checks" in {
        view(List("Transactor Name")).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors for applicant" must {
      "pass all a11y checks" in {
        view(List("Applicant Name")).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors for transactor and applicant" must {
      "pass all a11y checks" in {
        view(List("Applicant Name", "Transactor Name")).toString must passAccessibilityChecks
      }
    }
  }

}
