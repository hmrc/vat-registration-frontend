
package pages.vatapplication

import forms.vatapplication.AccountingPeriodForm
import helpers.A11ySpec
import models.api.vatapplication.FebruaryStagger
import views.html.vatapplication.AccountingPeriodView

class AccountingPeriodA11ySpec extends A11ySpec {

  val view: AccountingPeriodView = app.injector.instanceOf[AccountingPeriodView]

  "accounting period page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(AccountingPeriodForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(AccountingPeriodForm.form.fill(FebruaryStagger)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(AccountingPeriodForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
