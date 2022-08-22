

package pages.attachments

import helpers.A11ySpec
import views.html.attachments.DocumentsPost

class DocumentsPostA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[DocumentsPost]

  "the Documents Post page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
