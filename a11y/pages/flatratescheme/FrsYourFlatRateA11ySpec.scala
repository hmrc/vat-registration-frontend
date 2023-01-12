
package pages.flatratescheme

import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import helpers.A11ySpec
import views.html.flatratescheme.YourFlatRate

import java.text.DecimalFormat

class FrsYourFlatRateA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[YourFlatRate]
  val form = YesOrNoFormFactory.form()("frs.registerForWithSector")
  val decimalFormat = new DecimalFormat("#0.##")

  "the frs your flat rate page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view("test business type", decimalFormat.format(BigDecimal(10)), form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view("test business type", decimalFormat.format(BigDecimal(10)), form.fill(YesOrNoAnswer(true))).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view("test business type", decimalFormat.format(BigDecimal(10)), form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}