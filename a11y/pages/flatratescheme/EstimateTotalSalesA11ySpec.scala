
package pages.flatratescheme

import forms.EstimateTotalSalesForm
import helpers.A11ySpec
import views.html.flatratescheme.EstimateTotalSales

class EstimateTotalSalesA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[EstimateTotalSales]
  val form = EstimateTotalSalesForm.form

  "the estimate total sales page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill(BigDecimal(10))).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("totalSalesEstimate" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}