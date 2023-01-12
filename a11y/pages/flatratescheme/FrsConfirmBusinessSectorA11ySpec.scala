
package pages.flatratescheme

import helpers.A11ySpec
import views.html.flatratescheme.FrsConfirmBusinessSector

class FrsConfirmBusinessSectorA11ySpec extends A11ySpec {
  val view = app.injector.instanceOf[FrsConfirmBusinessSector]

  "the frs confirm business sector page" when {
    "the page is rendered" must {
      "pass all accessibility tests" in {
        view("Test BusinessType").toString must passAccessibilityChecks
      }
    }
  }
}