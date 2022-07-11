/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.applicant

import config.FrontendAppConfig
import controllers.applicant.{routes => applicantRoutes}
import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessVerificationStatus, BvPass, PartnershipIdEntity}
import models.{ApplicantDetails, Partner, PartnerEntity}
import play.api.libs.json.{Format, JsObject, JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import services.SessionService.{leadPartnerEntityKey, scottishPartnershipNameKey}

import scala.concurrent.Future

class PartnershipIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

  val partnershipJourneyUrl = "/partnership-identification/api/general-partnership-journey"
  val scottishPartnershipJourneyUrl = "/partnership-identification/api/scottish-partnership-journey"
  val retrieveDetailsUrl = s"/partnership-identification/api/journey/$testJourneyId"

  val testPostCode = "ZZ1 1ZZ"

  val testPartnershipResponse: JsObject = Json.obj(
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

  override val testPartnership: PartnershipIdEntity = PartnershipIdEntity(
    Some(testSautr),
    None,
    None,
    None,
    Some(testPostCode),
    testRegistration,
    Some(BvPass),
    Some(testSafeId),
    identifiersMatch = true
  )

  val testOtherCompanyName = "testOtherCompanyName"

  val partnershipApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testPartnership), roleInTheBusiness = Some(Partner))

  "GET /start-partnership-id-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided for Partnership" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(partnershipJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-partnership-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "return INTERNAL_SERVER_ERROR for invalid party type" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = GovOrg)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(partnershipJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-partnership-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "GET /partnership-id-callback" must {
    "redirect to the Task List" when {
      "the partnership is a General Partnership" when {
        "S4L model is not full" in new Setup {
          enable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testPartnership)))
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(roleInTheBusiness = Some(Partner)))
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))
            .registrationApi.getSection[ApplicantDetails](None)

          stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
          }
        }

        "the model in S4l is full" in new Setup {
          enable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(partnershipApplicantDetails)(ApplicantDetails.s4LWrites)
            .s4lContainer[ApplicantDetails].clearedByKey
            .registrationApi.replaceSection[ApplicantDetails](partnershipApplicantDetails)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

          stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
          }
        }
      }

      "redirect to the individual identification for Limited Liability Partnership" in new Setup {
        enable(TaskList)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(LtdLiabilityPartnership)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](None)
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testPartnership)))
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(roleInTheBusiness = Some(Partner)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = LtdLiabilityPartnership)))

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }
    }
    "redirect to the lead partner entity type page for Partnership" when {
      "S4L model is not full" in new Setup {
        disable(TaskList)

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testPartnership)))
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(roleInTheBusiness = Some(Partner)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))
          .registrationApi.getSection[ApplicantDetails](None)

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType.url)
        }
      }

      "the model in S4l is full" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
        given()
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].contains(partnershipApplicantDetails)(ApplicantDetails.s4LWrites)
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.replaceSection[ApplicantDetails](partnershipApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType.url)
        }
      }
    }

    "redirect to the individual identification for Limited Liability Partnership" in new Setup {
      disable(TaskList)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(LtdLiabilityPartnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testPartnership)))
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(roleInTheBusiness = Some(Partner)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = LtdLiabilityPartnership)))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
      }
    }
  }

  "GET /start-partnership-id-partner-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided for Partnership" in new Setup {
        given()
          .user.isAuthorised()

        insertIntoDb(sessionId, Map(
          leadPartnerEntityKey -> Json.toJson[PartyType](ScotPartnership),
          "CurrentProfile" -> Json.toJson(currentProfile)
        ))
        stubPost(scottishPartnershipJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-partnership-id-partner-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
        given().user.isAuthorised()
        insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

        stubPost(scottishPartnershipJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-partnership-id-partner-journey").get()

        whenReady(res) { result =>
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "GET /partnership-id-partner-callback" must {
    "redirect to the individual identification for Scottish Partnership" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testPartnership.copy(companyName = Some(testOtherCompanyName)), ScotPartnership, isLeadPartner = true))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](ScotPartnership),
        scottishPartnershipNameKey -> JsString(testOtherCompanyName),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
      }
    }

    "redirect to the individual identification for Scottish Limited Partnership" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testPartnership, ScotLtdPartnership, isLeadPartner = true))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](ScotLtdPartnership),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
      }
    }

    "redirect to the individual identification for Limited Liability Partnership" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testPartnership, LtdLiabilityPartnership, isLeadPartner = true))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertIntoDb(sessionId, Map(
        leadPartnerEntityKey -> Json.toJson[PartyType](ScotLtdPartnership),
        "CurrentProfile" -> Json.toJson(currentProfile)
      ))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
      }
    }

    "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.isUpdatedWithPartner(PartnerEntity(testPartnership, LtdLiabilityPartnership, isLeadPartner = true))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}