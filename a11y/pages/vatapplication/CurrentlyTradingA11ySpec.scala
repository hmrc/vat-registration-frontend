
package pages.vatapplication

import forms.vatapplication.CurrentlyTradingForm
import helpers.A11ySpec

import views.html.vatapplication.CurrentlyTradingView

class CurrentlyTradingA11ySpec extends A11ySpec {

  val regDate = "21 April 2022"
  val view: CurrentlyTradingView = app.injector.instanceOf[CurrentlyTradingView]

  "currently trading page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(CurrentlyTradingForm("past", regDate).form, "past", regDate).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(CurrentlyTradingForm("past", regDate).form.fill(true), "past", regDate).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(CurrentlyTradingForm("past", regDate).form.bind(Map("value" -> "")), "", "").body must passAccessibilityChecks
      }
    }
  }
}