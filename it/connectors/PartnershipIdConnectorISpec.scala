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

import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.Partnership
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass, PartnershipIdEntity}
import play.api.libs.json.{JsObject, JsResultException, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class PartnershipIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testPartnershipJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"
  val createPartnershipJourneyUrl = "/partnership-identification/api/general-partnership-journey"

  def retrieveDetailsUrl(journeyId: String) = s"/partnership-identification/api/journey/$journeyId"

  val connector: PartnershipIdConnector = app.injector.instanceOf[PartnershipIdConnector]

  val testJourneyConfig: PartnershipIdJourneyConfig = PartnershipIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out",
    accessibilityUrl = "/accessibility-url",
    regime = "VATC"
  )

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
    Some(testPostCode),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId for a Partnership" in {
        given()

        stubPost(createPartnershipJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.createJourney(testJourneyConfig, Partnership))

        res mustBe testJourneyUrl
      }

      "throw a JsResultException when the response JSON doesn't contain the journeyId" in {
        given()

        stubPost(createPartnershipJourneyUrl, CREATED, "{}")

        intercept[JsResultException] {
          await(connector.createJourney(testJourneyConfig, Partnership))
        }
      }
    }

    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        given()

        stubPost(createPartnershipJourneyUrl, UNAUTHORIZED, "")

        intercept[InternalServerException] {
          await(connector.createJourney(testJourneyConfig, Partnership))
        }
      }
    }
  }

  "getDetails" must {
    "return partnership when Partnership Id returns OK" in new Setup {
      given()

      stubGet(retrieveDetailsUrl(testPartnershipJourneyId), OK, Json.stringify(testPartnershipResponse))
      val res: PartnershipIdEntity = await(connector.getDetails(testPartnershipJourneyId))

      res mustBe testPartnership
    }

    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      given()

      val invalidTransactorJson: JsObject = testPartnershipResponse - "identifiersMatch"
      stubGet(retrieveDetailsUrl(testPartnershipJourneyId), OK, Json.stringify(Json.obj("personalDetails" -> invalidTransactorJson)))

      intercept[InternalServerException] {
        await(connector.getDetails(testPartnershipJourneyId))
      }
    }

    "throw an InternalServerException for any other status" in new Setup {
      given()

      stubGet(retrieveDetailsUrl(testPartnershipJourneyId), IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.getDetails(testPartnershipJourneyId))
      }
    }
  }
}
