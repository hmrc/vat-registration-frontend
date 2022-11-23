
package controllers.grs

import config.FrontendAppConfig
import itutil.ControllerISpec
import models.{PersonalDetails, TransactorDetails}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TransactorIdControllerISpec extends ControllerISpec {

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
    "dateOfBirth" -> testApplicantDob,
    "identifiersMatch" -> true
  )

  override val testPersonalDetails = PersonalDetails(
    firstName = testFirstName,
    lastName = testLastName,
    nino = Some(testApplicantNino),
    dateOfBirth = Some(testApplicantDob),
    identifiersMatch = true
  )

  "GET /start-sti-transactor-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(individualJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-transactor-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /sti-transactor-callback" when {
    "redirect to the task list" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](TransactorDetails(personalDetails = Some(testPersonalDetails)))
        .s4lContainer[TransactorDetails].clearedByKey

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-transactor-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

}