

package pages.fileupload

import helpers.A11ySpec
import views.html.fileupload.UploadDocumentTypeError

class UploadDocumentTypeErrorA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[UploadDocumentTypeError]

  "the Upload Document Type Error page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view().body must passAccessibilityChecks
      }
    }
  }

}
