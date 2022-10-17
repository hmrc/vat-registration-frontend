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

import controllers.grs.{routes => grsRoutes}
import featureswitch.core.config.{StubIncorpIdJourney, TaskList, UseSoleTraderIdentification}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api._
import models.external.IncorporatedEntity
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class IncorpIdControllerISpec extends ControllerISpec {

  val incorpDetailsJson: JsValue = Json.toJson(testIncorpDetails)(IncorporatedEntity.apiFormat)

  "GET /start-incorp-id-journey" should {
    "redirect to the returned journey url for UkCompany" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/limited-company-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.IncorpIdController.startJourney.url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for RegSociety" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = RegSociety)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.IncorpIdController.startJourney.url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for CharitableOrg" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = CharitableOrg)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.IncorpIdController.startJourney.url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /incorp-id-callback" when {
    "the Task List is enabled" when {
      "the UseSoleTraderIdentification feature switch is enabled and user is not transactor" should {
        "redirect to the Task List" in {
          enable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          enable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
          }
        }
      }

      "the UseSoleTraderIdentification feature switch is disabled and user is transactor" should {
        "redirect to the Task List" in {
          disable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          enable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
          }
        }
      }

      "the UseSoleTraderIdentification feature switch is disabled and user is not transactor" should {
        "redirect to the Task List" in {
          disable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          enable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
          }
        }
      }
    }
    "the Task List is disabled" when {
      "the UseSoleTraderIdentification feature switch is enabled and user is not transactor" should {
        "redirect to STI" in {
          enable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          disable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(grsRoutes.IndividualIdController.startJourney.url)
          }
        }
      }

      "the UseSoleTraderIdentification feature switch is disabled and user is transactor" should {
        "redirect to STI" in {
          disable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          disable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(grsRoutes.IndividualIdController.startJourney.url)
          }
        }
      }

      "the UseSoleTraderIdentification feature switch is disabled and user is not transactor" should {
        "redirect to PDV" in {
          disable(UseSoleTraderIdentification)
          disable(StubIncorpIdJourney)
          disable(TaskList)

          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
            .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
            .registrationApi.getSection[ApplicantDetails](None)
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

          stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

          val res = buildClient("/incorp-id-callback?journeyId=1").get()

          whenReady(res) { result =>
            result.status mustBe SEE_OTHER
            result.headers(LOCATION) must contain(controllers.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url)
          }
        }
      }
    }
  }
}
