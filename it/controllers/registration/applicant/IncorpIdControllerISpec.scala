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

import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{StubIncorpIdJourney, UseSoleTraderIdentification}
import itutil.ControllerISpec
import models.external.IncorporatedEntity
import models.{ApplicantDetails, S4LKey}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{CREATED, await, _}

class IncorpIdControllerISpec extends ControllerISpec {

  val incorpDetailsJson = Json.toJson(testIncorpDetails)(IncorporatedEntity.apiFormat)

  "GET /start-incorp-id-journey-limited-company" should {
    "redirect to the returned journey url" in new Setup {
      implicit val request = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl,  "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startLimitedCompanyJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /start-incorp-id-journey-registered-society" should {
    "redirect to the returned journey url" in new Setup {
      implicit val request = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl,  "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startRegisteredSocietyJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /incorp-id-callback" when {
    "when the UseSoleTraderIdentification feature switch is enabled" should {
      "redirect to STI" in {
        enable(UseSoleTraderIdentification)
        disable(StubIncorpIdJourney)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
          .vatScheme.has(ApplicantDetails.s4lKey.key, Json.toJson(ApplicantDetails()))
          .vatScheme.patched(ApplicantDetails.s4lKey.key, Json.obj())

        stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

        val res = buildClient("/incorp-id-callback?journeyId=1").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }
    }

    "when the UseSoleTraderIdentification feature switch is disabled" should {
      "redirect to PDV" in {
        disable(UseSoleTraderIdentification)
        disable(StubIncorpIdJourney)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails())
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))

        stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

        val res = buildClient("/incorp-id-callback?journeyId=1").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url)
        }
      }
    }
  }

}
