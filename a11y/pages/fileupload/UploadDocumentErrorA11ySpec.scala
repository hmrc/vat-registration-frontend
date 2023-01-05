

package pages.fileupload

import helpers.A11ySpec
import views.html.fileupload.UploadDocumentError

class UploadDocumentErrorA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[UploadDocumentError]

  "the Upload Document Error page" when {
    "the page is rendered with no errors" must {
      "pass all a11y checks" in {
        view().body must passAccessibilityChecks
      }
    }
  }

}
