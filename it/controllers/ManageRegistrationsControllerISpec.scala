
package controllers

import common.enums.VatRegStatus
import itutil.ControllerISpec
import models.api.VatSchemeHeader
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.RegistrationsApiStubs

class ManageRegistrationsControllerISpec extends ControllerISpec with RegistrationsApiStubs {

  val url = "/manage-registrations"

  def vatSchemeHeaderJson(regId: String, status: VatRegStatus.Value) = Json.toJson(VatSchemeHeader(
    registrationId = regId,
    status = status,
    applicationReference = Some(s"Application for $regId"),
    createdDate = Some(testCreatedDate),
    requiresAttachments = false
  ))

  "GET /manage-registrations" must {
    "return OK and present a list of only draft or submitted registrations" in new Setup {
      given.user.isAuthorised

      registrationsApi.GET.respondsWith(OK, Some(Json.arr(
        vatSchemeHeaderJson(testRegId, VatRegStatus.submitted),
        vatSchemeHeaderJson("2", VatRegStatus.draft))
      ))

      val res = await(buildClient(url).get)

      res.status mustBe OK
      Jsoup.parse(res.body).select("tr a").text mustBe s"Application for $testRegId Application for 2"
    }
  }

}
