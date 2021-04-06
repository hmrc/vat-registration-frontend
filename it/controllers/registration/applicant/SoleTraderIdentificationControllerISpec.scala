
package controllers.registration.applicant

import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.view.ApplicantDetails
import play.api.libs.json.Json
import play.api.test.Helpers._

class SoleTraderIdentificationControllerISpec extends ControllerISpec {

  val transactorDetailsJson = Json.obj("personalDetails" -> Json.toJson(testTransactorDetails))

  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val journeyUrl = "/sole-trader-identification/api/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

  "GET /start-sti-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided" in new Setup {
        given().user.isAuthorised

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
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())

      stubGet(retrieveDetailsUrl, OK, transactorDetailsJson.toString)
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
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].cleared
        .vatScheme.isUpdatedWith(validFullApplicantDetails)

      stubGet(retrieveDetailsUrl, OK, transactorDetailsJson.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
      }
    }
  }

}
