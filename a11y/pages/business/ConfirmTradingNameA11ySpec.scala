

package pages.business

import forms.ConfirmTradingNameForm
import helpers.A11ySpec
import views.html.business.ConfirmTradingNameView

class ConfirmTradingNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ConfirmTradingNameView]
  val form = ConfirmTradingNameForm.form
  val testCompanyName = "testCompanyName"
  val emptyCompanyName = ""

  "the HasWebsite page" when {
    "the page is rendered without errors when no company name is given" must {
      "pass all a11y checks" in {
        view(form, emptyCompanyName).body must passAccessibilityChecks
      }
    }
    "the page is rendered without errors when a company name is given" must {
      "pass all a11y checks" in {
        view(form, testCompanyName).body must passAccessibilityChecks
      }
    }
    "the page is rendered with errors for missing company name value" must {
      "pass all a11y checks" in {
        view(form.bind(Map("" -> "")), "").body must passAccessibilityChecks
      }
    }
  }

}
