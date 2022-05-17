
package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{Address, EligibilitySubmissionData, NETP, NonUkNonEstablished, UkCompany}
import models.external.{EmailAddress, EmailVerified, Name}
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{Format, JsBoolean, JsObject, JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class FormerNameControllerISpec extends ControllerISpec {

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
    formerName = Some(Name(Some(testFirstName),last = testLastName)),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, None)),
    roleInTheBusiness = Some(Director)
  )

  val url: String = controllers.applicant.routes.FormerNameController.show.url

  s"GET $url" must {
    "returns an OK" in new Setup {
      given()
        .user.isAuthorised()
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
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  "POST Former Name page" should {
    "patch Applicant Details in backend without former name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)(ApplicantDetails.s4LWrites)
        .registrationApi.replaceSection[ApplicantDetails](s4lData.copy(hasFormerName = Some(false), formerName = None, formerNameDate = None))
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map("value" -> Seq("false")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf.url)
      }
    }

    "Update backend with formerName and redirect to the Former Name Capture page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(hasFormerName = None))(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey(ApplicantDetails.s4lKey)
        .registrationApi.replaceSection[ApplicantDetails](s4lData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "value" -> "true"
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameCaptureController.show.url)
      }
    }

    "Update S4L with no formerName and redirect to the International Home Address page for NETP" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(NETP)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(hasFormerName = None, formerName = None))
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "value" -> "false"
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.InternationalHomeAddressController.show.url)
      }
    }

    "Update S4L with no formerName and redirect to the International Home Address page for Non UK Company" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(hasFormerName = Some(false), formerName = None))
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "value" -> "false"
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.InternationalHomeAddressController.show.url)
      }
    }
  }

}