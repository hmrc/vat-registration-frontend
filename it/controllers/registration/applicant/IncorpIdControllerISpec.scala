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
import featureswitch.core.config.StubIncorpIdJourney
import itutil.ControllerISpec
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{CREATED, await, _}

class IncorpIdControllerISpec extends ControllerISpec {

  "/start-incorp-id-journey" should {
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

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startIncorpIdJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

}
