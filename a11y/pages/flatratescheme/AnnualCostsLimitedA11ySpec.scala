
package pages.flatratescheme

import forms.OverBusinessGoodsPercentForm
import helpers.A11ySpec
import views.html.flatratescheme.AnnualCostsLimited

class AnnualCostsLimitedA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[AnnualCostsLimited]
  val form = new OverBusinessGoodsPercentForm {
    override val pct: BigDecimal = 10
  }.form
  val estimatedVatTurnover = BigDecimal(10)

  "the annual costs limited page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form, estimatedVatTurnover).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill(true), estimatedVatTurnover).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> "")), estimatedVatTurnover).toString must passAccessibilityChecks
      }
    }
  }
}