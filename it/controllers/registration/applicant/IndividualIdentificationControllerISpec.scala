
package controllers.registration.applicant

import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.EligibilitySubmissionData
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class IndividualIdentificationControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val individualJourneyUrl = "/sole-trader-identification/api/individual-journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

  val testSTIResponse: JsObject = Json.obj(
    "fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
    "nino" -> testApplicantNino,
    "dateOfBirth" -> testApplicantDob
  )

  "GET /start-sti-individual-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(individualJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-individual-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /sti-individual-callback" must {
    "redirect to the RoleInTheBusiness page" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-individual-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show.url)
      }
    }

    "redirect to the RoleInTheBusiness page when the model in S4l" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)(ApplicantDetails.writes)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-individual-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show.url)
      }
    }
  }
}