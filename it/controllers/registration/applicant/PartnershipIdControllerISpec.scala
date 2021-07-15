
package controllers.registration.applicant

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{Partnership, VatScheme}
import models.external.{BusinessVerificationStatus, BvPass, GeneralPartnership}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnershipIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val journeyUrl = "/partnership-identification/api/general-partnership/journey"
  val retrieveDetailsUrl = s"/partnership-identification/api/journey/$testJourneyId"

  val testPostCode = "ZZ1 1ZZ"

  val testPartnershipIdResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testPartnership: GeneralPartnership = GeneralPartnership(
    Some(testSautr),
    Some(testPostCode),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  val partnershipApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testPartnership))

  "GET /start-partnership-id-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.contains(fullVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(journeyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-partnership-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /partnership-id-callback" must {
    "redirect to the lead partner entity type page" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testPartnership)))
        .vatScheme.contains(
        VatScheme(
          id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership))
        )
      )

      stubGet(retrieveDetailsUrl, OK, testPartnershipIdResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType().url)
      }
    }

    "redirect to the lead partner entity type page when the model in S4l is full" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(partnershipApplicantDetails)
        .s4lContainer[ApplicantDetails].clearedByKey
        .vatScheme.isUpdatedWith(partnershipApplicantDetails)
        .vatScheme.contains(
        VatScheme(
          id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership))
        ))

      stubGet(retrieveDetailsUrl, OK, testPartnershipIdResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType().url)
      }
    }
  }

}