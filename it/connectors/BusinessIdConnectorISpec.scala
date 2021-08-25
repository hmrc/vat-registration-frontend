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
import models.api.{Trust, UnincorpAssoc}
import models.external.businessid.BusinessIdJourneyConfig
import models.external.{BusinessIdEntity, BusinessVerificationStatus, BvPass}
import play.api.libs.json.{JsObject, JsResultException, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class BusinessIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testUnincorpAssocJourneyId = "1"
  val testTrustJourneyId = "2"
  val testJourneyUrl = "/test-journey-url"
  val createTrustJourneyUrl = "/business-identification/api/trust/journey"
  val createUnincorpAssocJourneyUrl = "/business-identification/api/unincorporated-association/journey"

  def retrieveDetailsUrl(journeyId: String) = s"/business-identification/api/journey/$journeyId"

  val connector: BusinessIdConnector = app.injector.instanceOf[BusinessIdConnector]

  val testJourneyConfig: BusinessIdJourneyConfig = BusinessIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out"
  )

  val testPostCode = "ZZ1 1ZZ"

  val testTrustResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "chrn" -> testChrn,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testTrust: BusinessIdEntity = BusinessIdEntity(
    Some(testSautr),
    Some(testPostCode),
    Some(testChrn),
    None,
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  val testUnincorpAssocResponse: JsObject = Json.obj(
    "sautr" -> testSautr,
    "postcode" -> testPostCode,
    "chrn" -> testChrn,
    "casc" -> testCasc,
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testUnincorpAssoc: BusinessIdEntity = BusinessIdEntity(
    Some(testSautr),
    Some(testPostCode),
    Some(testChrn),
    Some(testCasc),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId for a Trust" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createTrustJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.createJourney(testJourneyConfig, Trust))

        res mustBe testJourneyUrl
      }

      "return the journey ID when the response JSON includes the journeyId for a Unincorporated Association" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createUnincorpAssocJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.createJourney(testJourneyConfig, UnincorpAssoc))

        res mustBe testJourneyUrl
      }

      "throw a JsResultException when the response JSON doesn't contain the journeyId" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createTrustJourneyUrl, CREATED, "{}")

        intercept[JsResultException] {
          await(connector.createJourney(testJourneyConfig, Trust))
        }
      }
    }

    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createUnincorpAssocJourneyUrl, UNAUTHORIZED, "")

        intercept[InternalServerException] {
          await(connector.createJourney(testJourneyConfig, Trust))
        }
      }
    }
  }

  "getDetails" must {
    "return trust when Business Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, Json.stringify(testTrustResponse))
      val res: BusinessIdEntity = await(connector.getDetails(testTrustJourneyId))

      res mustBe testTrust
    }

    "return unincorporated association when Business Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, Json.stringify(testUnincorpAssocResponse))
      val res: BusinessIdEntity = await(connector.getDetails(testUnincorpAssocJourneyId))

      res mustBe testUnincorpAssoc
    }

    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val invalidTransactorJson: JsObject = testTrustResponse - "identifiersMatch"
      stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, Json.stringify(Json.obj("personalDetails" -> invalidTransactorJson)))

      intercept[InternalServerException] {
        await(connector.getDetails(testTrustJourneyId))
      }
    }

    "throw an InternalServerException for any other status" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testTrustJourneyId), IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.getDetails(testTrustJourneyId))
      }
    }
  }
}
