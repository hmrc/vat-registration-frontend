

package pages.attachments

import helpers.A11ySpec
import models.api._
import views.html.attachments.MultipleDocumentsRequired

class MultipleDocumentsRequiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[MultipleDocumentsRequired]

  "the Multiple Documents Required page" when {
    "the page is rendered without errors when attachment list contains multiple attachments" must {
      "pass all a11y checks" in {
        view(List(IdentityEvidence, VAT2, VAT5L, TaxRepresentativeAuthorisation), None, None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when attachment list contains only identity evidence" must {
      "pass all a11y checks" in {
        view(List(IdentityEvidence), None, None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when attachment list contains only VAT2" must {
      "pass all a11y checks" in {
        view(List(VAT2), None, None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when attachment list contains only VAT5L" must {
      "pass all a11y checks" in {
        view(List(VAT5L), None, None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when attachment list contains only TaxRepresentativeAuthorisation" must {
      "pass all a11y checks" in {
        view(List(TaxRepresentativeAuthorisation), None, None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when transactor is unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(List(TransactorIdentityEvidence), None, Some("Transactor Name")).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when applicant is unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(List(IdentityEvidence), Some("Applicant Name"), None).toString must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when transactor and applicant are unverified in the transactor flow" must {
      "pass all a11y checks" in {
        view(List(IdentityEvidence, TransactorIdentityEvidence), Some("Applicant Name"), Some("Transactor Name")).toString must passAccessibilityChecks
      }
    }
  }

}
