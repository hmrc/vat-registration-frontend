
package pages.vatapplication

import forms.vatapplication.MandatoryDateForm
import helpers.A11ySpec
import models.DateSelection
import play.api.data.Form
import utils.MessageDateFormat
import views.html.vatapplication.MandatoryStartDateIncorpView

import java.time.LocalDate

class MandatoryStartDateIncorpA11ySpec extends A11ySpec {

  val view: MandatoryStartDateIncorpView = app.injector.instanceOf[MandatoryStartDateIncorpView]

  val incorpDate: LocalDate = LocalDate.now.minusYears(3)
  val calculatedDate: LocalDate = LocalDate.now().minusYears(2).withMonth(12).withDayOfMonth(12)
  val formattedDate: String = MessageDateFormat.format(calculatedDate)

  val form: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(incorpDate, calculatedDate)

  "mandatory start date incorp page" when {
    "rendered with no value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form, formattedDate).body must passAccessibilityChecks
      }
    }

    "rendered with value and there are no form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("value" -> DateSelection.calculated_date.toString)), formattedDate).body must passAccessibilityChecks
      }
    }

    "there are form errors" must {
      "pass all a11y checks" in {
        view(form.bind(Map("" -> "")), formattedDate).body must passAccessibilityChecks
      }
    }
  }
}