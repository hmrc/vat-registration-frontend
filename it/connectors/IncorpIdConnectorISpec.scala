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
import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.{CharitableOrg, PartyType, RegSociety, UkCompany}
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import models.external.{BvFail, BvPass, IncorporatedEntity}
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs

class IncorpIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching with ITRegistrationFixtures {

  lazy val connector: IncorpIdConnector = app.injector.instanceOf[IncorpIdConnector]
  val testIncorpId = "testIncorpId"

  "createLimitedCompanyJourney" when {
    "the stub Incorp ID feature switch is enabled" should {
      "call the test only route to stub the journey for UkCompany" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        enable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(UkCompany)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, UkCompany))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for RegSociety" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        enable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(RegSociety)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, RegSociety))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for CharitableOrg" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        enable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(CharitableOrg)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, CharitableOrg))

        res mustBe testJourneyStartUrl
      }
    }

    "the stub Incorp ID feature switch is disabled" should {
      "call the create Incorp ID journey API for UkCompany" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        disable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/limited-company-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, UkCompany))

        res mustBe testJourneyStartUrl
      }

      "call the create Incorp ID journey API for RegSociety" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        disable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, RegSociety))

        res mustBe testJourneyStartUrl
      }

      "call the create Incorp ID journey API for CharitableOrg" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        disable(StubIncorpIdJourney)

        val testJourneyConfig = IncorpIdJourneyConfig(continueUrl = "/test", deskProServiceId = "vrs", signOutUrl = "/signOutUrl", accessibilityUrl = "/accessibility")
        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, CharitableOrg))

        res mustBe testJourneyStartUrl
      }
    }
  }

  "getDetails" when {
    "incorp ID returns valid incorporation details" should {
      "return the incorporation details without optional data" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = false, "REGISTRATION_FAILED", BvFail, None)
        disable(StubIncorpIdJourney)
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }

      "return the incorporation details with optional data" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = true, "REGISTERED", BvPass, Some(testBpSafeId))
        disable(StubIncorpIdJourney)
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }

      "return the incorporation details for a charitable organisation" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), None, Some(testChrn), testIncorpDate, "GB", identifiersMatch = true, "REGISTERED", BvPass, Some(testBpSafeId))
        disable(StubIncorpIdJourney)
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }
    }
    "incorp ID returns invalid incorporation details" should {
      "throw and exception" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        disable(StubIncorpIdJourney)
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, "")

        intercept[Exception](await(connector.getDetails(testIncorpId)))
      }
    }
  }

}
