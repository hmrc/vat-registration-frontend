
package pages.errors

import helpers.A11ySpec
import views.html.errors.BankDetailsLockoutView

class BankDetailsLockoutA11ySpec extends A11ySpec {

  val bankDetailsLockoutView: BankDetailsLockoutView = app.injector.instanceOf[BankDetailsLockoutView]


}
