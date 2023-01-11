
package pages.flatratescheme

import forms.ChooseBusinessTypeForm
import helpers.A11ySpec
import models.{FrsBusinessType, FrsGroup}
import views.html.flatratescheme.ChooseBusinessType

class ChooseBusinessTypeA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[ChooseBusinessType]
  val form = ChooseBusinessTypeForm.form(Seq("020", "019", "038"))
  val frsGroups =  Seq(FrsGroup(
  label = "Test 1",
  labelCy = "Test 1",
  categories = List(
    FrsBusinessType(id = "020", label = "Hotel or accommodation", labelCy = "Hotel or accommodation", percentage = 10.5),
    FrsBusinessType(id = "019", label = "Test BusinessType", labelCy = "Hotel or accommodation", percentage = 3),
    FrsBusinessType(id = "038", label = "Pubs", labelCy = "Hotel or accommodation", percentage = 5)
  ))
)

  "the choose business type page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form, frsGroups).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill("020"), frsGroups).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> "")), frsGroups).toString must passAccessibilityChecks
      }
    }
  }
}