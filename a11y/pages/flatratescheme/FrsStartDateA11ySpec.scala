
package pages.flatratescheme

import forms.FRSStartDateForm
import helpers.A11ySpec
import models.FRSDateChoice
import views.html.flatratescheme.frs_start_date

import java.time.LocalDate

class FrsStartDateA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[frs_start_date]
  val startDate = LocalDate.of(2021, 6, 30)
  val endDate = LocalDate.of(2021, 8, 30)
  val form = FRSStartDateForm.form(startDate, endDate)

  "the frs start date page" when {
    "the page is rendered without errors when no value is given" must {
      "pass all accessibility tests" in {
        view(form, startDate.toString).toString must passAccessibilityChecks
      }
    }

    "the page is rendered without errors when a value is given" must {
      "pass all accessibility tests" in {
        view(form.fill((FRSDateChoice.VATDate, Some(startDate))), startDate.toString).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing value" must {
      "pass all accessibility tests" in {
        view(form.bind(Map("value" -> "")), startDate.toString).toString must passAccessibilityChecks
      }
    }
  }
}