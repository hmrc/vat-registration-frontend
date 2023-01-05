

package pages.fileupload

import helpers.A11ySpec
import models.api.PrimaryIdentityEvidence
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.twirl.api.Html
import views.html.fileupload.UploadingDocument

import java.time.LocalDateTime

class UploadingDocumentA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[UploadingDocument]
  lazy val testView: Html = view(UpscanDetails(
    attachmentType = PrimaryIdentityEvidence,
    reference = "testReference",
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  ))

  "the Uploading Document page" when {
    "the page is rendered with no errors" must {
      "pass all a11y checks" in {
        testView.body must passAccessibilityChecks
      }
    }
  }

}
