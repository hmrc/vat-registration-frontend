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

import featuretoggle.FeatureSwitch.StubIncorpIdJourney
import featuretoggle.FeatureToggleSupport
import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api._
import models.external._
import models.external.incorporatedentityid.{IncorpIdJourneyConfig, JourneyLabels, TranslationLabels}
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, SessionId}


class IncorpIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureToggleSupport with ITRegistrationFixtures {

  lazy val connector: IncorpIdConnector = app.injector.instanceOf[IncorpIdConnector]
  val testIncorpId = "testIncorpId"

  val testJourneyConfig = IncorpIdJourneyConfig(
    continueUrl = "/test",
    deskProServiceId = "vrs",
    signOutUrl = "/signOutUrl",
    accessibilityUrl = "/accessibility",
    regime = "VATC",
    businessVerificationCheck = true,
    labels = Some(JourneyLabels(
      en = TranslationLabels(optServiceName = Some("MTD")),
      cy = TranslationLabels(optServiceName = Some("MTD"))
    ))
  )

  "createLimitedCompanyJourney" when {
    "the stub Incorp ID feature switch is enabled" should {
      "call the test only route to stub the journey for UkCompany" in {
        given()

        enable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(UkCompany)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, UkCompany)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for RegSociety" in {
        given()

        enable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(RegSociety)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, RegSociety)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for CharitableOrg" in {
        given()

        enable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(CharitableOrg)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, CharitableOrg)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for Individual" in {
        given()

        enable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(Individual)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val exception = intercept[InternalServerException](await(connector.createJourney(testJourneyConfig, Individual)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly)))

        exception.getMessage must include("Party type Individual is not a valid incorporated entity party type")
      }
    }

    "the stub Incorp ID feature switch is disabled" should {
      "call the create Incorp ID journey API for UkCompany" in {
        given()

        disable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/limited-company-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, UkCompany))

        res mustBe testJourneyStartUrl
      }

      "call the create Incorp ID journey API for RegSociety" in {
        given()

        disable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, RegSociety))

        res mustBe testJourneyStartUrl
      }

      "call the create Incorp ID journey API for CharitableOrg" in {
        given()

        disable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val res = await(connector.createJourney(testJourneyConfig, CharitableOrg))

        res mustBe testJourneyStartUrl
      }

      "call the test only route to stub the journey for Individual" in {
        given()

        disable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(Individual)}", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val exception = intercept[InternalServerException](await(connector.createJourney(testJourneyConfig, Individual)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly)))

        exception.getMessage must include("Party type Individual is not a valid incorporated entity party type")
      }

      "call the test only route to stub the journey for CharitableOrg" in {
        given()

        disable(StubIncorpIdJourney)

        val testJourneyStartUrl = "/test"
        val testDeskProServiceId = "vrs"

        stubPost(s"/register-for-vat/test-only/api/incorp-id-journey\\?partyType=${PartyType.stati(CharitableOrg)}", INTERNAL_SERVER_ERROR, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

        val exception = intercept[InternalServerException](await(connector.createJourney(testJourneyConfig, CharitableOrg)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly)))

        exception.getMessage must include("Invalid response from incorporated entity identification for CharitableOrg: Status:")
      }
    }
  }

  "getDetails" when {
    "incorp ID returns valid incorporation details" should {
      "return the incorporation details when the StubIncorpIdJourney FS is enabled" in {
        enable(StubIncorpIdJourney)
        given().user.isAuthorised()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = false, FailedStatus, Some(BvFail), None)
        stubGet(s"/register-for-vat/test-only/api/incorp-id-journey/testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId)(HeaderCarrier(sessionId = Some(SessionId(sessionString))), implicitly))

        res mustBe validResponse
      }
      "return the incorporation details without optional data" in {
        disable(StubIncorpIdJourney)
        given().user.isAuthorised()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = false, FailedStatus, Some(BvFail), None)
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }

      "return the incorporation details with optional data" in {
        disable(StubIncorpIdJourney)
        given().user.isAuthorised()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = true, RegisteredStatus, Some(BvPass), Some(testBpSafeId))
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }

      "return the incorporation details for a charitable organisation" in {
        disable(StubIncorpIdJourney)
        given().user.isAuthorised()

        val validResponse = IncorporatedEntity(testCrn, Some(testCompanyName), None, Some(testChrn), testIncorpDate, "GB", identifiersMatch = true, RegisteredStatus, Some(BvPass), Some(testBpSafeId))
        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson(validResponse)(IncorporatedEntity.apiFormat).toString)

        val res = await(connector.getDetails(testIncorpId))

        res mustBe validResponse
      }
    }
    "incorp ID returns invalid incorporation details" should {
      "throw and exception" in {
        disable(StubIncorpIdJourney)
        given()

        stubGet(s"/incorporated-entity-identification/api/journey/$testIncorpId", CREATED, Json.toJson("").toString())

        val exception = intercept[Exception](await(connector.getDetails(testIncorpId)))
        exception.getMessage must include("Incorp ID returned invalid JSON")
      }
    }
  }

}
