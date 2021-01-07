
package controllers.registration.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.view.ApplicantDetails
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class CaptureRoleInTheBusinessControllerISpec extends ControllerISpec {

  "GET /role-in-the-business" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/role-in-the-business").get)

      res.status mustBe OK

    }
  }

  "POST /role-in-the-business" when {
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

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(""))

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
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
          .s4lContainer[ApplicantDetails].cleared

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(""))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.FormerNameController.show().url)
      }
    }
  }
}