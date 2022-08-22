

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.EmailDocuments

class EmailDocumentsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[EmailDocuments]

  "the Email Documents page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
