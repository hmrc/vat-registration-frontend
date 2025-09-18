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
import featuretoggle.FeatureSwitch.StubIncorpIdJourney
import itutil.ControllerISpec
import models.Entity
import models.api._
import models.external.IncorporatedEntity
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerIncorpIdControllerISpec extends ControllerISpec {

  val incorpDetailsJson: JsValue = Json.toJson(testIncorpDetails)(IncorporatedEntity.apiFormat)

  "GET /partner/index/start-incorp-id-journey" must {
    "redirect to task list if the lead partner questions are not started yet" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(grsRoutes.PartnerIncorpIdController.startJourney(1).url).get())

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return INTERNAL_SERVER_ERROR if no partyType set" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getListSection(Some(List(Entity(None, UkCompany, Some(true), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(grsRoutes.PartnerIncorpIdController.startJourney(2).url).get())

      res.status mustBe INTERNAL_SERVER_ERROR
    }

    "redirect to the returned journey url for UkCompany" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getListSection(Some(List(Entity(None, UkCompany, Some(true), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/limited-company-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.PartnerIncorpIdController.startJourney(1).url).get())

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for RegSociety" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getListSection(Some(List(Entity(None, RegSociety, Some(true), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.PartnerIncorpIdController.startJourney(1).url).get())

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for CharitableOrg" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getListSection(Some(List(Entity(None, CharitableOrg, Some(true), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.PartnerIncorpIdController.startJourney(1).url).get())

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /partner/index/incorp-id-callback" when {
    "redirect to the Task List" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getSection(Some(Entity(None, UkCompany, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection(Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None), idx = Some(1))

      stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(routes.PartnerIncorpIdController.callback(1, "1").url).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
      }
    }
    "the index is bigger than 1" must {
      "redirect to ALF for partner flow" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(Entity(None, UkCompany, Some(false), None, None, None, None)), idx = Some(2))
          .registrationApi.replaceSection(Entity(Some(testIncorpDetails), UkCompany, Some(false), None, None, None, None), idx = Some(2))

        stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: Future[WSResponse] = buildClient(routes.PartnerIncorpIdController.callback(2, "1").url).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(controllers.partners.routes.PartnerAddressController.redirectToAlf(2).url)
        }
      }
    }
  }

}
