
package controllers.grs

import config.FrontendAppConfig
import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessVerificationStatus, BvPass}
import models.{ApplicantDetails, Entity}
import play.api.libs.json.{Format, JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerSoleTraderIdControllerISpec extends ControllerISpec {

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
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    )
  )

  "GET /start-sti-partner-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided when the applicant is a Sole Trader" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatScheme)
          .registrationApi.getSection(Some(Entity(None, Individual, Some(true), None)), idx = Some(1))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-partner-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
      "redirect to the journey using the ID provided when the applicant is a NETP" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatScheme)
          .registrationApi.getSection(Some(Entity(None, NETP, Some(true), None)), idx = Some(1))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-partner-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatScheme)

        insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))
        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-sti-partner-journey").get()

        whenReady(res) { result =>
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "GET /sti-partner-callback" must {
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection(Some(Entity(None, Individual, Some(true), None)), idx = Some(1))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), Individual, Some(true), None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }
    "redirect to the FormerName page if the user is a NETP" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection(Some(Entity(None, NETP, Some(true), None)), idx = Some(1))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), NETP, Some(true), None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }

    "redirect to the FormerName page when the model in S4l is full and the user is a Sole Trader" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails.copy(entity = Some(testPartnership)))(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection(Some(Entity(None, Individual, Some(true), None)), idx = Some(1))
        .registrationApi.replaceSection(validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), Individual, Some(true), None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }
    "redirect to the FormerName page when the model in S4l is full and the user is a NETP" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails.copy(entity = Some(testPartnership)))(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection(Some(Entity(None, NETP, Some(true), None)), idx = Some(1))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), NETP, Some(true), None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }

    "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails.copy(entity = Some(testPartnership)))(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), NETP, isLeadPartner = Some(true), None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}