
package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{Address, EligibilitySubmissionData, UkCompany}
import models.external.{EmailAddress, EmailVerified, Name}
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class FormerNameCaptureControllerISpec extends ControllerISpec {

  val email = "test@t.test"
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)

  val s4lData = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    hasFormerName = Some(true),
    formerName = Some(Name(Some(testFirstName), last = testLastName)),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, None)),
    roleInTheBusiness = Some(Director)
  )

  val url: String = controllers.applicant.routes.FormerNameCaptureController.show.url

  s"GET $url" must {
    "returns an OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "returns an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)(ApplicantDetails.s4LWrites)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("formerFirstName").attr("value") mustBe testFirstName
        Jsoup.parse(res.body).getElementById("formerLastName").attr("value") mustBe testLastName
      }
    }
  }

  "POST Former Name page" should {
    "Update backend with formerName and redirect to the Former Name Date page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(formerName = None))(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey(ApplicantDetails.s4lKey)
        .registrationApi.replaceSection[ApplicantDetails](s4lData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/what-was-previous-name").post(Map(
        "formerFirstName" -> testFirstName,
        "formerLastName" -> testLastName
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show.url)
      }
    }
  }
}