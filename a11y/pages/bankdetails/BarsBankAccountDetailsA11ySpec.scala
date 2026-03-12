
package pages.bankdetails

import forms.EnterBankAccountDetailsForm
import helpers.A11ySpec
import models.BankAccountDetails
import play.api.data.Form
import views.html.bankdetails.EnterBankAccountDetails

class BarsBankAccountDetailsA11ySpec extends A11ySpec {

  val view: EnterBankAccountDetails = app.injector.instanceOf[EnterBankAccountDetails]
  val form: Form[BankAccountDetails] = EnterBankAccountDetailsForm.form

  "the Enter Company Bank Account Details page" when {
    "there are no form errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }
    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}