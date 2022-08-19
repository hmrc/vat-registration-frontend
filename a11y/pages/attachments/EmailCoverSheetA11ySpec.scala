

package pages.attachments

import helpers.A11ySpec
import models.api._
import views.html.attachments.EmailCoverSheet

class EmailCoverSheetA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[EmailCoverSheet]
  val testRef = "VRN12345689"
  val testAttachments: List[AttachmentType] = List[AttachmentType](VAT2, VAT51, IdentityEvidence, VAT5L, TaxRepresentativeAuthorisation)
  val testVat2: List[AttachmentType] = List[AttachmentType](VAT2)

  "the Email Cover Sheet page" when {
    "the page is rendered without errors when attachment list contains all attachment types" must {
      "pass all a11y checks" in {
        view(testRef, testAttachments, None, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when attachment list contains only VAT2" must {
      "pass all a11y checks" in {
        view(testRef, testVat2, None, None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when transactor is unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(testRef, List(TransactorIdentityEvidence), None, Some("Transactor Name")).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when applicant is unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(testRef, List(IdentityEvidence), Some("Applicant Name"), None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when transactor and applicant both are unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(testRef, List(IdentityEvidence, TransactorIdentityEvidence), Some("Applicant Name"), Some("Transactor Name")).toString must passAccessibilityChecks
      }
    }
  }

}
