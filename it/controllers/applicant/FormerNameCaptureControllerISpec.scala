
package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.external.Name
import models.{ApplicantDetails, FormerName}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class FormerNameCaptureControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.FormerNameCaptureController.show.url

  s"GET $url" must {
    "returns an OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "returns an OK with prepopulated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("formerFirstName").attr("value") mustBe testFormerFirstName
        Jsoup.parse(res.body).getElementById("formerLastName").attr("value") mustBe testFormerLastName
      }
    }
  }

  "POST Former Name page" must {
    "Update backend with formerName and redirect to the Former Name Date page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(changeOfName = FormerName())))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(changeOfName = FormerName(name = Some(Name(Some(testFirstName), last = testLastName)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/what-was-previous-name").post(Map(
        "formerFirstName" -> testFirstName,
        "formerLastName" -> testLastName
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show.url)
      }
    }

    "return form with errors for invalid name" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/what-was-previous-name").post(Map(
        "formerFirstName" -> "",
        "formerLastName" -> ""
      ))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}