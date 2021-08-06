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

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{Partnership, Trust, UnincorpAssoc, VatScheme}
import models.external.{BusinessIdEntity, BusinessVerificationStatus, BvPass}
import models.{ApplicantDetails, Partner}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testUnincorpAssocJourneyId = "1"
  val testTrustJourneyId = "2"
  val testJourneyUrl = "/test-journey-url"
  val createTrustJourneyUrl = "/business-identification/api/trust/journey"
  val createUnincorpAssocJourneyUrl = "/business-identification/api/unincorporated-association/journey"

  def retrieveDetailsUrl(journeyId: String) = s"/business-identification/api/journey/$journeyId"

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

  val testTrust: BusinessIdEntity = BusinessIdEntity(
    Some(testSautr),
    Some(testPostCode),
    Some(testChrn),
    None,
    testRegistration,
    BvPass,
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

  val testUnincorpAssoc: BusinessIdEntity = BusinessIdEntity(
    Some(testSautr),
    Some(testPostCode),
    Some(testChrn),
    Some(testCasc),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  val unincorpAssocApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(entity = Some(testUnincorpAssoc))

  "GET /start-business-id-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided for Trust" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.contains(fullVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Trust))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(createTrustJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-business-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "redirect to the journey using the ID provided for Unincorporated Association" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.contains(fullVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        stubPost(createUnincorpAssocJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient("/start-business-id-journey").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
    }
  }

  "GET /business-id-callback" must {
    "redirect to the lead business entity type page for Trust" when {
      "S4L model is not full" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testTrust)))
          .vatScheme.contains(
          VatScheme(
            id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Trust))
          )
        )

        stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, testTrustResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/business-id-callback?journeyId=$testTrustJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }

      "the model in S4l is full" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(trustApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.isUpdatedWith(trustApplicantDetails)
          .vatScheme.contains(
          VatScheme(
            id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Trust))
          ))

        stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, testTrustResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/business-id-callback?journeyId=$testTrustJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }
    }

    "redirect to the lead business entity type page for Unincorporated Association" when {
      "S4L model is not full" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testUnincorpAssoc)))
          .vatScheme.contains(
          VatScheme(
            id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc))
          )
        )

        stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, testUnincorpAssocResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/business-id-callback?journeyId=$testUnincorpAssocJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }

      "the model in S4l is full" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(unincorpAssocApplicantDetails)
          .s4lContainer[ApplicantDetails].clearedByKey
          .vatScheme.isUpdatedWith(unincorpAssocApplicantDetails)
          .vatScheme.contains(
          VatScheme(
            id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UnincorpAssoc))
          ))

        stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, testUnincorpAssocResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/business-id-callback?journeyId=$testUnincorpAssocJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }
    }
  }

}