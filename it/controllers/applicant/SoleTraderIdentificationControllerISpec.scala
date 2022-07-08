
package controllers.applicant

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.applicant.{routes => applicantRoutes}
import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessVerificationStatus, BvPass}
import models.{ApplicantDetails, PartnerEntity}
import play.api.libs.json.{Format, JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import services.SessionService.leadPartnerEntityKey

import scala.concurrent.Future

class SoleTraderIdentificationControllerISpec extends ControllerISpec {

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

  "GET /start-sti-journey" when {
    "STI returns a journey ID when user is not transactor" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
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
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP, isTransactor = true)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
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
        enable(TaskList)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](None)
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

        stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }

      "S4l is full and the user is a Sole Trader" in new Setup {
        enable(TaskList)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)(ApplicantDetails.s4LWrites)
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

        stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }
    }
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      disable(TaskList)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }

    "redirect to the FormerName page when the model in S4l is full and the user is a Sole Trader" in new Setup {
      disable(TaskList)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }

    "throw an exception if the user is not a Sole Trader or NETP" in new Setup {
      disable(TaskList)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Individual)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData)
        )
      )

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "GET /start-sti-partner-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided when the applicant is a Sole Trader" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(fullVatScheme)

        insertIntoDb(sessionId, Map(
          leadPartnerEntityKey -> Json.toJson[PartyType](Individual),
          "CurrentProfile" -> Json.toJson(currentProfile)
        ))
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
          .vatScheme.contains(fullVatScheme)

        insertIntoDb(sessionId, Map(
          leadPartnerEntityKey -> Json.toJson[PartyType](NETP),
          "CurrentProfile" -> Json.toJson(currentProfile)
        ))
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
          .vatScheme.contains(fullVatScheme)

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
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](Individual),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

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
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testSoleTrader, NETP, isLeadPartner = true))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](NETP),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

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
        .registrationApi.replaceSection(validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](Individual),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

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
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testSoleTrader, NETP, isLeadPartner = true))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](NETP),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

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
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testSoleTrader, NETP, isLeadPartner = true))
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