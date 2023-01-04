
package controllers.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, RegSociety, UkCompany}
import models.{ApplicantDetails, OtherDeclarationCapacity}
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureRoleInTheBusinessControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.CaptureRoleInTheBusinessController.show.url

  val otherRole = "test"

  s"GET $url" must {
    "show the view" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "director"
      }
    }

    "show the view with prepopulated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").first().`val`() mustBe "director"
      }
    }

    "show the view with prepopulated data for Other" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(
        roleInTheBusiness = Some(OtherDeclarationCapacity),
        otherRoleInTheBusiness = Some(otherRole)
      )))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = RegSociety)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").first().`val`() mustBe "other"
        Jsoup.parse(res.body).getElementById("otherRole").`val`() mustBe otherRole
      }
    }
  }

  s"POST $url" when {
    "applicant details has invalid role" must {
      "return BAD_REQUEST" in new Setup {
        disable(StubEmailVerification)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/role-in-the-business").post(Map("value" -> "boardMember")))

        res.status mustBe BAD_REQUEST
      }
    }

    "post to the backend and redirect former name page" in new Setup {
      disable(StubEmailVerification)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(roleInTheBusiness = None)))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient("/role-in-the-business").post(Map("value" -> "director")))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(routes.FormerNameController.show.url)
    }

    "post to the backend and redirect former name page for Other" in new Setup {
      disable(StubEmailVerification)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(roleInTheBusiness = None)))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(
        roleInTheBusiness = Some(OtherDeclarationCapacity),
        otherRoleInTheBusiness = Some(otherRole)
      ))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = RegSociety)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient("/role-in-the-business").post(Map(
        "value" -> "other",
        "otherRole" -> otherRole
      )))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(routes.FormerNameController.show.url)
    }
  }
}