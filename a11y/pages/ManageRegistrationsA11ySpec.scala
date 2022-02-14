
package pages

import common.enums.VatRegStatus
import helpers.A11ySpec
import models.api.VatSchemeHeader
import views.html.ManageRegistrations

import java.time.LocalDate

class ManageRegistrationsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ManageRegistrations]

  val testRegistrationList = List(
    VatSchemeHeader(
      registrationId = testRegId,
      status = VatRegStatus.draft,
      applicationReference = Some("reference 1"),
      createdDate = Some(LocalDate.now),
      requiresAttachments = false
    ),
    VatSchemeHeader(
      registrationId = testRegId + "2",
      status = VatRegStatus.draft,
      applicationReference = Some("reference 2"),
      createdDate = Some(LocalDate.now),
      requiresAttachments = true
    ),
    VatSchemeHeader(
      registrationId = testRegId + "3",
      status = VatRegStatus.submitted,
      applicationReference = Some("reference 1"),
      createdDate = Some(LocalDate.now),
      requiresAttachments = false
    )
  )

  "the Manage Registrations page" must {
    "pass all accessibility checks" in {
      view(testRegistrationList).toString must passAccessibilityChecks
    }
  }

}
