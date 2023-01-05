

package pages.fileupload

import forms.RemoveUploadedDocumentForm
import helpers.A11ySpec
import play.api.mvc.Call
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.RemoveUploadedDocument

class RemoveUploadedDocumentA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[RemoveUploadedDocument]

  val testReference = "reference"
  val testDocumentName = "testDocumentName"

  val removeLink: Call = controllers.fileupload.routes.RemoveUploadedDocumentController.submit(testReference)
  val testList = List(DocumentUploadSummaryRow("test-document", removeLink))

  "the RemoveUploadedDocuments page" when {
    "the page is rendered without errors" must {
      "pass all a11y checks" in {
        view(RemoveUploadedDocumentForm(testDocumentName).form, testReference, testDocumentName).body must passAccessibilityChecks
      }
    }
  }

}
