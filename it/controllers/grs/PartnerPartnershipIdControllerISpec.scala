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

package controllers.grs

import config.FrontendAppConfig
import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api._
import models.external.{BusinessVerificationStatus, BvPass, PartnershipIdEntity}
import models.{ApplicantDetails, Entity, Partner}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerPartnershipIdControllerISpec extends ControllerISpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"

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

  "GET /start-partnership-id-partner-journey" when {
    "STI returns a journey ID" must {
      "redirect to the journey using the ID provided for Partnership" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), Some(testOtherCompanyName))), idx = Some(1))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

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
    "redirect to the correct controller for Scottish Partnership" in new Setup {
      private def verifyCallbackHandler(redirectUrl: String) = {
        val details = testPartnership.copy(companyName = Some(testOtherCompanyName))
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Entity](Some(Entity(Some(testPartnership), ScotPartnership, Some(true), Some(testOtherCompanyName))), idx = Some(1))
          .registrationApi.replaceSection(Entity(Some(details), ScotPartnership, Some(true), Some(testOtherCompanyName)), idx = Some(1))

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(redirectUrl)
        }
      }

      enable(TaskList)
      verifyCallbackHandler(controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyCallbackHandler(controllers.grs.routes.IndividualIdController.startJourney.url)
    }

    "redirect to correct controller for Scottish Limited Partnership" in new Setup {
      private def verifyCallbackHandler(redirectUrl: String) = {
        val entity = Entity(None, ScotLtdPartnership, Some(true), Some("company name"))

        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(entity), idx = Some(1))
          .registrationApi.replaceSection(entity.copy(details = Some(testPartnership)), idx = Some(1))

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(redirectUrl)
        }
      }

      enable(TaskList)
      verifyCallbackHandler(controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyCallbackHandler(controllers.grs.routes.IndividualIdController.startJourney.url)
    }

    "redirect to the individual identification for Limited Liability Partnership" in new Setup {
      private def verifyCallbackHandler(redirectUrl: String) = {
        val entity = Entity(None, LtdLiabilityPartnership, Some(true), Some("company name"))

        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(entity), idx = Some(1))
          .registrationApi.replaceSection(entity.copy(details = Some(testPartnership)), idx = Some(1))

        stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(redirectUrl)
        }
      }

      enable(TaskList)
      verifyCallbackHandler(controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyCallbackHandler(controllers.grs.routes.IndividualIdController.startJourney.url)
    }

    "return INTERNAL_SERVER_ERROR if not party type available" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection(Entity(Some(testPartnership), LtdLiabilityPartnership, Some(true), None))

      stubGet(retrieveDetailsUrl, OK, testPartnershipResponse.toString)
      insertIntoDb(sessionId, Map("CurrentProfile" -> Json.toJson(currentProfile)))

      val res: Future[WSResponse] = buildClient(s"/register-for-vat/partnership-id-partner-callback?journeyId=$testJourneyId").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}