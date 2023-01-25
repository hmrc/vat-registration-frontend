
package pages.vatapplication

import forms.AnnualStaggerForm
import helpers.A11ySpec
import models.api.vatapplication.JanDecStagger
import views.html.vatapplication.LastMonthOfAccountingYear

class LastMonthOfAccountingYearA11ySpec extends A11ySpec {

  val view: LastMonthOfAccountingYear = app.injector.instanceOf[LastMonthOfAccountingYear]

  "last month of accounting year page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(AnnualStaggerForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(AnnualStaggerForm.form.fill(JanDecStagger)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(AnnualStaggerForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}