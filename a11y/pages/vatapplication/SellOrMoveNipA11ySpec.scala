
package pages.vatapplication

import forms.SellOrMoveNipForm
import helpers.A11ySpec
import views.html.vatapplication.SellOrMoveNip

class SellOrMoveNipA11ySpec extends A11ySpec {

  val view: SellOrMoveNip = app.injector.instanceOf[SellOrMoveNip]

  "sell or move nip page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SellOrMoveNipForm.form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(SellOrMoveNipForm.form.fill((true, Some(100)))).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(SellOrMoveNipForm.form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }

    "there are form errors when incomplete values submitted" must {
      "pass all a11y checks" in {
        view(SellOrMoveNipForm.form.bind(Map("value" -> "true"))).body must passAccessibilityChecks
      }
    }
  }
}