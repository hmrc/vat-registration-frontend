
package pages.vatapplication

import forms.VoluntaryStartDateNoChoiceForm
import helpers.A11ySpec
import services.TimeService
import views.html.vatapplication.VoluntaryStartDateNoChoice

import java.time.LocalDate

class VoluntaryStartDateNoChoiceA11ySpec extends A11ySpec {

  val timeService: TimeService = app.injector.instanceOf[TimeService]
  val form = new VoluntaryStartDateNoChoiceForm(timeService)
  val view: VoluntaryStartDateNoChoice = app.injector.instanceOf[VoluntaryStartDateNoChoice]

  "voluntary start date no choice page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form(), "example date").body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form().fill(LocalDate.now()), "example date").body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(form().bind(Map(
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        )), "example date").body must passAccessibilityChecks
      }
    }
  }
}