
package pages.vatapplication

import forms.StoringGoodsForm
import helpers.A11ySpec
import models.api.vatapplication.{StoringGoodsForDispatch, StoringWithinUk}
import play.api.data.Form
import views.html.vatapplication.StoringGoods

class StoringGoodsA11ySpec extends A11ySpec {

  val view: StoringGoods = app.injector.instanceOf[StoringGoods]
  val form: Form[StoringGoodsForDispatch] = new StoringGoodsForm().form

  "storing goods page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form.fill(StoringWithinUk)).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("value" -> ""))).body must passAccessibilityChecks
      }
    }
  }
}