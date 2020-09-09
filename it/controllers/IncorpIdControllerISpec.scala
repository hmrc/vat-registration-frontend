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

package controllers

import featureswitch.core.config.{FeatureSwitching, StubIncorpIdJourney}
import itutil.IntegrationSpecBase
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{CREATED, await, _}
import support.AppAndStubs

class IncorpIdControllerISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching with IntegrationPatience {
  "/start-incorp-id-journey" should {
    "redirect to the returned journey url" in new StandardTestHelpers {
      implicit val request = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"

      stubPost("/incorporated-entity-identification/api/journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl).toString)

      val res: WSResponse = await(buildClient(controllers.routes.IncorpIdController.startIncorpIdJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)

    }
  }
}
