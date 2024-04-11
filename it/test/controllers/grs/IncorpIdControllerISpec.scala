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
import models.ApplicantDetails
import models.api._
import models.external.IncorporatedEntity
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class IncorpIdControllerISpec extends ControllerISpec {

  val incorpDetailsJson: JsValue = Json.toJson(testIncorpDetails)(IncorporatedEntity.apiFormat)

  "GET /start-incorp-id-journey" must {
    "redirect to the returned journey url for UkCompany" in new Setup {
      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(grsRoutes.IncorpIdController.startJourney.url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /incorp-id-callback" when {
    "redirect to the Task List" in {
      disable(StubIncorpIdJourney)

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](ApplicantDetails(entity = Some(testIncorpDetails)))
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
