/*
 * Copyright 2022 HM Revenue & Customs
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

  def getUrl(index: Int = 1): String = routes.PartnerSoleTraderIdController.startJourney(index).url

  val callbackUrl: String = routes.PartnerSoleTraderIdController.callback(1, testJourneyId).url

  s"GET ${getUrl()}" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided when the applicant is a Sole Trader" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))
          .registrationApi.getListSection(Some(List(Entity(None, Individual, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient(getUrl()).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }
      "redirect to the journey using the ID provided when the applicant is a NETP" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))
          .registrationApi.getListSection(Some(List(Entity(None, NETP, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost(soleTraderJourneyUrl, CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString())

        val res: Future[WSResponse] = buildClient(getUrl()).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(testJourneyUrl)
        }
      }

      "redirect to task list if questions have been answered for lead partner" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))

        insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

        val res: Future[WSResponse] = buildClient(getUrl()).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }

      "return INTERNAL_SERVER_ERROR if no party type available" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))
          .registrationApi.getListSection(Some(List(Entity(None, NETP, Some(true), None, None, None, None))))

        insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

        val res: Future[WSResponse] = buildClient(getUrl(2)).get()

        whenReady(res) { result =>
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  s"GET $callbackUrl" must {
    "redirect to the FormerName page if the user is a Sole Trader" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection(Some(Entity(None, Individual, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(callbackUrl).get()

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
        .registrationApi.getSection(Some(Entity(None, NETP, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), NETP, Some(true), None, None, None, None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(callbackUrl).get()

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
        .registrationApi.getSection(Some(Entity(None, Individual, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection(validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(callbackUrl).get()

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
        .registrationApi.getSection(Some(Entity(None, NETP, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(entity = Some(testPartnership)))
        .registrationApi.replaceSection(Entity(Some(testSoleTrader), NETP, Some(true), None, None, None, None), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(callbackUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(applicantRoutes.FormerNameController.show.url)
      }
    }

    "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))

      stubGet(retrieveDetailsUrl, OK, testSTIResponse.toString)
      insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

      val res: Future[WSResponse] = buildClient(callbackUrl).get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}