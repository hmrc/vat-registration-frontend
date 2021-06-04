
package controllers.registration.applicant

import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.ApplicantDetails
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._

class SoleTraderIdentificationControllerISpec extends ControllerISpec {

  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val journeyUrl = "/sole-trader-identification/api/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

  val testSTIResponse: JsObject = Json.obj(
    "fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
    "nino" -> testApplicantNino,
    "dateOfBirth" -> testApplicantDob
  )

  "GET /start-sti-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.contains(fullVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(journeyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res = buildClient("/start-sti-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /sti-callback" must {
    "redirect to the CaptureRoleInTheBusiness page" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
      }
    }
    "redirect to the CaptureRoleInTheBusiness page when the model in S4l is full" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
      }
    }
  }

}
