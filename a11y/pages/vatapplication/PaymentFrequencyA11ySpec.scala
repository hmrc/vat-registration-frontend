
package pages.vatapplication

import forms.PaymentFrequencyForm
import helpers.A11ySpec
import models.api.vatapplication.MonthlyPayment
import views.html.vatapplication.PaymentFrequencyView

class PaymentFrequencyA11ySpec extends A11ySpec {

  val view: PaymentFrequencyView = app.injector.instanceOf[PaymentFrequencyView]

  "payment frequency page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(PaymentFrequencyForm()).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(PaymentFrequencyForm().fill(MonthlyPayment)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(PaymentFrequencyForm().bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}