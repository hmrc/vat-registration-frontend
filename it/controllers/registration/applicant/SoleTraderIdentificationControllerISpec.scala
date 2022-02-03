
package controllers.registration.applicant

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessVerificationStatus, BvPass}
import models.{ApplicantDetails, PartnerEntity}
import play.api.libs.json.{JsObject, Json}
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
          .user.isAuthorised
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
          .user.isAuthorised
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
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
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
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)(ApplicantDetails.writes)
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
      given()
        .user.isAuthorised
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
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
          .user.isAuthorised
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
          .user.isAuthorised
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
    }
  }

  "GET /sti-partner-callback" must {
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
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
      given()
        .user.isAuthorised
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
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
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)(ApplicantDetails.writes)
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
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)(ApplicantDetails.writes)
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
  }

}