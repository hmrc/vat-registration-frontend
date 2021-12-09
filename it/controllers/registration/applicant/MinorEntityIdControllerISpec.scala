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

package controllers.registration.applicant

import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{EligibilitySubmissionData, NonUkNonEstablished, Trust, UnincorpAssoc}
import models.external.soletraderid.OverseasIdentifierDetails
import models.external.{BusinessVerificationStatus, BvPass, MinorEntity}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class MinorEntityIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testUnincorpAssocJourneyId = "1"
  val testTrustJourneyId = "2"
  val testNonUkCompanyJourneyId = "3"
  val testJourneyUrl = "/test-journey-url"
  val createTrustJourneyUrl = "/minor-entity-identification/api/trusts-journey"
  val createUnincorpAssocJourneyUrl = "/minor-entity-identification/api/unincorporated-association-journey"
  val createNonUkCompanyJourneyUrl = "/minor-entity-identification/api/overseas-company-journey"

  def retrieveDetailsUrl(journeyId: String) = s"/minor-entity-identification/api/journey/$journeyId"

  val testPostCode = "ZZ1 1ZZ"

  val testTrustResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "chrn" -> testChrn,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testTrust: MinorEntity = MinorEntity(
    None,
    Some(testSautr),
    None,
    None,
    Some(testPostCode),
    Some(testChrn),
    None,
    testRegistration,
    Some(BvPass),
    Some(testSafeId),
    identifiersMatch = true
  )

  val trustApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testTrust))

  val testUnincorpAssocResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "chrn" -> testChrn,
    "casc" -> testCasc,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testUnincorpAssoc: MinorEntity = MinorEntity(
    None,
    Some(testSautr),
    None,
    None,
    Some(testPostCode),
    Some(testChrn),
    Some(testCasc),
    testRegistration,
    Some(BvPass),
    Some(testSafeId),
    identifiersMatch = true
  )

  val unincorpAssocApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testUnincorpAssoc))

  val testOverseasIdentifier = "1234567890"
  val testOverseasIdentifierCountry = "EE"
  val testOverseasIdentifierDetails = OverseasIdentifierDetails(testOverseasIdentifier, testOverseasIdentifierCountry)

  val testNonUkCompanyResponse: JsObject = Json.obj(
    "ctutr" -> testCrn,
    "overseas" -> Json.obj(
      "taxIdentifier" -> testOverseasIdentifier,
      "country" -> testOverseasIdentifierCountry
    ),
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testNonUkCompany: MinorEntity = MinorEntity(
    None,
    None,
    Some(testCrn),
    Some(testOverseasIdentifierDetails),
    None,
    None,
    None,
    testRegistration,
    Some(BvPass),
    Some(testSafeId),
    identifiersMatch = true
  )

  val nonUkCompanyApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testNonUkCompany))

  "GET /start-minor-entity-id-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided for Trust" in new Setup {
        given()
          .user.isAuthorised
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Trust)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(createTrustJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-minor-entity-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "redirect to the journey using the ID provided for Unincorporated Association" in new Setup {
        given()
          .user.isAuthorised
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(createUnincorpAssocJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-minor-entity-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "redirect to the journey using the ID provided for Non UK Company" in new Setup {
        given()
          .user.isAuthorised
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(createNonUkCompanyJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-minor-entity-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /minor-entity-id-callback" must {
    "redirect to the lead business entity type page for Trust" when {
      "S4L model is not full" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testTrust)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Trust)))

        stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, testTrustResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testTrustJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }

      "the model in S4l is full" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(trustApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.isUpdatedWith(trustApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Trust)))

        stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, testTrustResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testTrustJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }
    }

    "redirect to the lead business entity type page for Unincorporated Association" when {
      "S4L model is not full" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testUnincorpAssoc)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc)))

        stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, testUnincorpAssocResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testUnincorpAssocJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }

      "the model in S4l is full" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(unincorpAssocApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.isUpdatedWith(unincorpAssocApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc)))

        stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, testUnincorpAssocResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testUnincorpAssocJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }
    }

    "redirect to the lead business entity type page for Non UK Company" when {
      "S4L model is not full" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testNonUkCompany)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

        stubGet(retrieveDetailsUrl(testNonUkCompanyJourneyId), OK, testNonUkCompanyResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testNonUkCompanyJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }

      "the model in S4l is full" in new Setup {
        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(nonUkCompanyApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.isUpdatedWith(nonUkCompanyApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

        stubGet(retrieveDetailsUrl(testNonUkCompanyJourneyId), OK, testNonUkCompanyResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/minor-entity-id-callback?journeyId=$testNonUkCompanyJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.IndividualIdentificationController.startJourney.url)
        }
      }
    }
  }

}