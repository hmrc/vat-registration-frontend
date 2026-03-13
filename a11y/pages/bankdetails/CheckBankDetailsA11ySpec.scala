package pages.bankdetails

import helpers.A11ySpec
import models.BankAccountDetails
import views.html.bankdetails.CheckBankDetailsView

class CheckBankDetailsA11ySpec extends A11ySpec {

  val view: CheckBankDetailsView             = app.injector.instanceOf[CheckBankDetailsView]
  val bankAccountDetails: BankAccountDetails = BankAccountDetails("Test Name", "12345678", "123456", None)

  "the Check Bank Details page" when {
    "displaying bank details" must {
      "pass all a11y checks" in {
        view(bankAccountDetails).body must passAccessibilityChecks
      }
    }
    "displaying bank details with a roll number" must {
      "pass all a11y checks" in {
        view(bankAccountDetails.copy(rollNumber = Some("AB/121212"))).body must passAccessibilityChecks
      }
    }
  }

}
