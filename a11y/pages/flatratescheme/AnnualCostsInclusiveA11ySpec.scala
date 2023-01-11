
package pages.flatratescheme

import forms.OverBusinessGoodsForm
import helpers.A11ySpec
import views.html.flatratescheme.AnnualCostsInclusive

class AnnualCostsInclusiveA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[AnnualCostsInclusive]
  val form = OverBusinessGoodsForm.form

  "the annual costs inclusive page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill(true)).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> ""))).toString must passAccessibilityChecks
      }
    }
  }
}