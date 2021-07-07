

package controllers.registration.applicant

import controllers.Assets.{NOT_IMPLEMENTED, OK}
import itutil.ControllerISpec
import models.ApplicantDetails
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class LeadPartnerEntityControllerISpec extends ControllerISpec {

  //TO DO To be updated when new API is implemented

  val url: String = controllers.registration.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType().url

  s"GET $url" should {
    "display the page" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "Z1"
      }
    }
  }

  s"POST $url" when {
    val keyblock = "applicant-details"
    "the user selects Sole Trader" should {
      "post to the backend and begin a STI journey" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
          .s4lContainer[ApplicantDetails].clearedByKey

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/lead-partner-entity").post(Map("value" -> "Z1")))

        res.status mustBe NOT_IMPLEMENTED  //TO DO To be updated when new API has been implemented
      }
    }
  }
}
