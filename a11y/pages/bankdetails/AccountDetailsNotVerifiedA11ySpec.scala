package pages.bankdetails

import helpers.A11ySpec
import views.html.bankdetails.AccountDetailsNotVerifiedView

class AccountDetailsNotVerifiedA11ySpec extends A11ySpec {

  val accountDetailsNotVerifiedView: AccountDetailsNotVerifiedView = app.injector.instanceOf[AccountDetailsNotVerifiedView]

  "the AccountDetailsNotVerifiedView page with 1 attempt used" must {
    "pass all a11y checks" in {
      accountDetailsNotVerifiedView(1).body must passAccessibilityChecks
    }
  }

  "the AccountDetailsNotVerifiedView page with 2 attempts used" must {
    "pass all a11y checks" in {
      accountDetailsNotVerifiedView(2).body must passAccessibilityChecks
    }
  }

}
