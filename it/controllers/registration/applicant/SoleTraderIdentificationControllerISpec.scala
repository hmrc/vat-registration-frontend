
package controllers.registration.applicant

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{Individual, UkCompany, VatScheme}
import models.external.incorporatedentityid.{BusinessVerificationStatus, BvPass, SoleTrader}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._

class SoleTraderIdentificationControllerISpec extends ControllerISpec {

  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val journeyUrl = "/sole-trader-identification/api/journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

  val testSautr = "1234567890"
  val testRegistration = "REGISTERED"
  val testSafeId = "X00000123456789"

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

  val testSoleTrader: SoleTrader = SoleTrader(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = testApplicantNino,
    sautr = Some(testSautr),
    registration = testRegistration,
    businessVerification = BvPass,
    bpSafeId = Some(testSafeId),
    identifiersMatch = true
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
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show().url)
      }
    }

    "redirect to the RoleInTheBusiness page if the user is not a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))
        )
      )

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
      }
    }

    "redirect to the CaptureRoleInTheBusiness page when the model in S4l is full and the user is not a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))
        ))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
      }
    }

    "redirect to the FormerName page when the model in S4l is full and the user is a Sole Trader" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(validFullApplicantDetails)
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        ))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(s"/register-for-vat/sti-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show().url)
      }
    }
  }

}