
package pages.vatapplication

import forms.vatapplication.VoluntaryDateForm
import helpers.A11ySpec
import models.DateSelection
import services.TimeService
import views.html.vatapplication.StartDateIncorp

class StartDateIncorpA11ySpec extends A11ySpec {

  val view: StartDateIncorp = app.injector.instanceOf[StartDateIncorp]

  val timeService = app.injector.instanceOf[TimeService]
  val dateMin = timeService.today
  val dateMax = timeService.today
  val form = VoluntaryDateForm.form(dateMin, dateMax).fill((DateSelection.specific_date, Some(dateMin)))

  val registeredDate = timeService.dynamicFutureDateExample()
  val incorpDateAfter = true
  val dateExample = timeService.dynamicFutureDateExample()

  "mandatory start date incorp page" when {

    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form, registeredDate, incorpDateAfter, dateExample).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        val data = Map(
          "value" -> "company_registration_date",
          "startDate" -> ""
        )
        view(form.bind(data), registeredDate, incorpDateAfter, dateExample).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        val data = Map(
          "value" -> "specific_date",
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        )
        view(form.bind(data), registeredDate, incorpDateAfter, dateExample).body must passAccessibilityChecks
      }
    }
  }
}