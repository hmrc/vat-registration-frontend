
package controllers.registration.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.external.{EmailAddress, EmailVerified}
import models.{ApplicantDetails, Director}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureRoleInTheBusinessControllerISpec extends ControllerISpec {

  val url: String = controllers.registration.applicant.routes.CaptureRoleInTheBusinessController.show().url

  val s4lData = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    roleInTheBusiness = Some(Director)
  )

  s"GET $url" should {
    "show the view with prepopulated data" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .audit.writesAuditMerged()
        .vatScheme.contains(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "director"
      }
    }
  }

  s"POST $url" when {
    val keyblock = "applicant-details"
    "the ApplicantDetails model is incomplete" should {
      "update S4L and redirect to former name page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy())
          .vatScheme.contains(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "director")))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.FormerNameController.show().url)
      }
    }

    "the ApplicantDetails model is complete" should {
      "post to the backend and redirect former name page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.contains(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "director")))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.FormerNameController.show().url)
      }
    }
  }
}