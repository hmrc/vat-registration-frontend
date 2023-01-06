
package pages.vatapplication

import forms.PaymentFrequencyForm
import helpers.A11ySpec
import models.api.vatapplication.MonthlyPayment
import views.html.vatapplication.payment_frequency

class PaymentFrequencyA11ySpec extends A11ySpec {

  val view: payment_frequency = app.injector.instanceOf[payment_frequency]

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