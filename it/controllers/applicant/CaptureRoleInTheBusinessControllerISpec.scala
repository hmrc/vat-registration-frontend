
package controllers.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.external.{EmailAddress, EmailVerified}
import models.{ApplicantDetails, Director, Trustee}
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureRoleInTheBusinessControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.CaptureRoleInTheBusinessController.show.url

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
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)(ApplicantDetails.s4LWrites)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "director"
      }
    }
  }

  s"POST $url" when {
    "applicant details has invalid role" should {
      "return BAD_REQUEST" in new Setup {
        disable(StubEmailVerification)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .registrationApi.getSection[ApplicantDetails](None)
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(roleInTheBusiness = Some(Trustee)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "trustee")))
        res.status mustBe BAD_REQUEST
      }
    }

    "the ApplicantDetails model is incomplete" should {
      "update S4L and redirect to former name page" in new Setup {
        disable(StubEmailVerification)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .registrationApi.getSection[ApplicantDetails](None)
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(roleInTheBusiness = Some(Director)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "director")))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.FormerNameController.show.url)
      }
    }

    "the ApplicantDetails model is complete" should {
      "post to the backend and redirect former name page" in new Setup {
        disable(StubEmailVerification)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)(ApplicantDetails.s4LWrites)
          .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "director")))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.FormerNameController.show.url)
      }
    }
  }
}