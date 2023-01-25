
package pages.vatapplication

import forms.PaymentMethodForm
import helpers.A11ySpec
import models.api.vatapplication.StandingOrder
import views.html.vatapplication.AasPaymentMethod

class AasPaymentMethodA11ySpec extends A11ySpec {

  val view: AasPaymentMethod = app.injector.instanceOf[AasPaymentMethod]

  "aas payment method page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(PaymentMethodForm()).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(PaymentMethodForm().fill(StandingOrder)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(PaymentMethodForm().bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }

}
