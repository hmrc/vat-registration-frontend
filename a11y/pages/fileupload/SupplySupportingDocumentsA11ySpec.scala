

package pages.fileupload

import forms.SupplySupportingDocumentsForm
import helpers.A11ySpec
import views.html.fileupload.SupplySupportingDocuments


class SupplySupportingDocumentsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[SupplySupportingDocuments]
  val form = SupplySupportingDocumentsForm.form

  "the Supply Supporting Documents page" when {
    "the page is rendered with no errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
  }

}
