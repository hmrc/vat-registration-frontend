

package pages.business

import forms.TradingNameForm
import helpers.A11ySpec
import views.html.business.trading_name

class TradingNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[trading_name]
  val form = TradingNameForm.form
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
