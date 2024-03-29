
package controllers.grs

import config.FrontendAppConfig
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessRegistrationStatus, BusinessVerificationStatus, BvPass}
import models.{ApplicantDetails, OwnerProprietor}
import play.api.libs.json.{Format, JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class SoleTraderIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val soleTraderJourneyUrl = "/sole-trader-identification/api/sole-trader-journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

  val testSTIResponse: JsObject = Json.obj(
    "fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
    "nino" -> testApplicantNino,
    "dateOfBirth" -> testApplicantDob,
    "sautr" -> testSautr,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> Json.toJson[BusinessRegistrationStatus](testRegistration),
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  "GET /start-sti-journey" when {
    "STI returns a journey ID when user is not transactor" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
          partyType = NETP,
          fixedEstablishmentInManOrUk = false
        )))

        insertCurrentProfileIntoDb(currentProfile, sessionString)
        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
    "STI returns a journey ID when user is transactor" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
          partyType = NETP,
          isTransactor = true,
          fixedEstablishmentInManOrUk = false
        )))

        insertCurrentProfileIntoDb(currentProfile, sessionString)
        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /sti-callback" must {
    "redirect to the Task List" when {
      "the user is a Sole Trader" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(personalDetails = None, entity = None)))
          .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(entity = None))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = None)))
          .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(
          entity = Some(testSoleTrader),
          roleInTheBusiness = Some(OwnerProprietor)
        ))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

        stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }
    }
  }

}