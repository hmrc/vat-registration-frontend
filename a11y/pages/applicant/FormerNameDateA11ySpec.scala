
package pages.applicant

import forms.FormerNameDateForm
import helpers.A11ySpec
import views.html.applicant.FormerNameDate

import java.time.LocalDate

class FormerNameDateA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[FormerNameDate]
  val form = FormerNameDateForm.form(LocalDate.now())

  "the former name change date page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form, "former-name", None).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form.bind(Map.empty[String, String]), "former-name", None).toString must passAccessibilityChecks
      }
    }
  }
}