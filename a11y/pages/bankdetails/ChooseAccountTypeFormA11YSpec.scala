package pages.bankdetails

import forms.ChooseAccountTypeForm
import helpers.A11ySpec
import models.bars.BankAccountType
import play.api.data.Form
import views.html.bankdetails.ChooseAccountTypeView

class ChooseAccountTypeFormA11YSpec extends A11ySpec {

  val view: ChooseAccountTypeView = app.injector.instanceOf[ChooseAccountTypeView]
  val form: Form[BankAccountType] = ChooseAccountTypeForm.form

  "the ChooseAccountTypeView page" must {
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
