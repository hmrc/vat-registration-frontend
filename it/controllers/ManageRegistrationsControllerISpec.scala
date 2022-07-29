
package controllers

import common.enums.VatRegStatus
import itutil.ControllerISpec
import models.api.VatSchemeHeader
import org.jsoup.Jsoup
import play.api.test.Helpers._

class ManageRegistrationsControllerISpec extends ControllerISpec {

  val url = "/manage-registrations"

  def vatSchemeHeader(regId: String, status: VatRegStatus.Value): VatSchemeHeader = VatSchemeHeader(
    registrationId = regId,
    status = status,
    applicationReference = Some(s"Application for $regId"),
    createdDate = testCreatedDate,
    requiresAttachments = false
  )

  "GET /manage-registrations" must {
    "return OK and present a list of only draft or submitted registrations" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getAllRegistrations(List(
        vatSchemeHeader(testRegId, VatRegStatus.submitted),
        vatSchemeHeader("2", VatRegStatus.draft)
      ))

      val res = await(buildClient(url).get)

      res.status mustBe OK
      Jsoup.parse(res.body).select("tr a").text mustBe s"Application for $testRegId Application for 2"
    }
  }

}
