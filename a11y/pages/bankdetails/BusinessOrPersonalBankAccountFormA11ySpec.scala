package pages.bankdetails

import forms.BusinessOrPersonalBankAccountForm
import helpers.A11ySpec
import models.bars.BankAccountType
import play.api.data.Form
import views.html.bankdetails.BusinessOrPersonalBankAccountView

class BusinessOrPersonalBankAccountFormA11ySpec extends A11ySpec {

  val view: BusinessOrPersonalBankAccountView = app.injector.instanceOf[BusinessOrPersonalBankAccountView]
  val form: Form[BankAccountType] = BusinessOrPersonalBankAccountForm.form

  "the BusinessOrPersonalBankAccountView page" must {
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
