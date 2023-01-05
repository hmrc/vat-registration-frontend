

package pages.fileupload

import helpers.A11ySpec
import models.api.PrimaryIdentityEvidence
import models.external.upscan.UpscanResponse
import play.twirl.api.Html
import views.html.fileupload.UploadDocument

class UploadDocumentA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[UploadDocument]

  val testReference = "testReference"
  val testHref = "testHref"
  val testHint = "testHint"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  "the Upload Document page" when {
    "the page is rendered with no errors and no error response from upscan" must {
      "pass all a11y checks" in {
        view(testUpscanResponse, Some(Html(testHint)), PrimaryIdentityEvidence, None).body must passAccessibilityChecks
      }
    }
    "the page is rendered with no errors and an error response from upscan" must {
      "pass all a11y checks" in {
        view(testUpscanResponse, Some(Html(testHint)), PrimaryIdentityEvidence, Some("EntityTooLarge")).body must passAccessibilityChecks
      }
    }
  }

}
