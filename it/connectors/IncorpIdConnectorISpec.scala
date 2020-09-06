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

package connectors

import featureswitch.core.config.{FeatureSwitching, StubIncorpIdJourney}
import itutil.IntegrationSpecBase
import models.IncorpIdJourneyConfig
import play.api.libs.json.Json
import support.AppAndStubs
import play.api.test.Helpers._

class IncorpIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching {

  lazy val connector: IncorpIdConnector = app.injector.instanceOf[IncorpIdConnector]

  "createJourney" when {
    "the stub Incorp ID feature switch is enabled" should {
      "call the test only route to stub the journey" in {
        enable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig("/test")
        val testJourneyStartUrl = "/test"

        stubPost("/test-only/api/incorp-id-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl).toString)

        val res = await(connector.createJourney(testJourneyConfig))

        res mustBe testJourneyStartUrl
      }
    }

    "the stub Incorp ID feature switch is disabled" should {
      "call the create Incorp ID journey API" in {
        disable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig("/test")
        val testJourneyStartUrl = "/test"

        stubPost("/incorporated-entity-identification/api/journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl).toString)

        val res = await(connector.createJourney(testJourneyConfig))

        res mustBe testJourneyStartUrl
      }
    }
  }
}