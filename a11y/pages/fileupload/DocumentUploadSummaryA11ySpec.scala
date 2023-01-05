

package pages.fileupload

import forms.DocumentUploadSummaryForm
import helpers.A11ySpec
import models.api.IdentityEvidence
import play.api.mvc.Call
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.DocumentUploadSummary

class DocumentUploadSummaryA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[DocumentUploadSummary]
  val form = DocumentUploadSummaryForm.form
  val testReference = "reference"
  val testDocument = "test-document"

  val removeLink: Call = controllers.fileupload.routes.RemoveUploadedDocumentController.submit(testReference)

  val testList = List(DocumentUploadSummaryRow(testDocument, removeLink))
  val testEmptyList = List()

  "the Document Upload Summary page" when {
    "the page is rendered without errors with one document uploaded and pending" must {
      "pass all a11y checks" in {
        view(DocumentUploadSummaryForm.form, testList, testList.size, supplySupportingDocuments = false).body must passAccessibilityChecks
      }
    }
    "the page is rendered without errors with one document uploaded and pending and supplying supporting documents" must {
      "pass all a11y checks" in {
        view(DocumentUploadSummaryForm.form, testList, testList.size, supplySupportingDocuments = true).body must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when there are no documents uploaded or pending" must {
      "pass all a11y checks" in {
        view(DocumentUploadSummaryForm.form, testEmptyList, testEmptyList.size, supplySupportingDocuments = false).body must passAccessibilityChecks
      }
    }
  }

}
