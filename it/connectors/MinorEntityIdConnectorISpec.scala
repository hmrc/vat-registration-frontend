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
import models.api.{NonUkNonEstablished, Trust, UnincorpAssoc}
import models.external.minorentityid.MinorEntityIdJourneyConfig
import models.external.soletraderid.OverseasIdentifierDetails
import models.external.{BusinessVerificationStatus, BvPass, MinorEntity}
import play.api.libs.json.{JsObject, JsResultException, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class MinorEntityIdConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testUnincorpAssocJourneyId = "1"
  val testTrustJourneyId = "2"
  val testNonUkCompanyJourneyId = "3"
  val testJourneyUrl = "/test-journey-url"
  val createTrustJourneyUrl = "/minor-entity-identification/api/trust-journey"
  val createUnincorpAssocJourneyUrl = "/minor-entity-identification/api/unincorporated-association-journey"
  val createNonUkCompanyJourneyUrl = "/minor-entity-identification/api/overseas-company-journey"

  def retrieveDetailsUrl(journeyId: String) = s"/minor-entity-identification/api/journey/$journeyId"

  val connector: MinorEntityIdConnector = app.injector.instanceOf[MinorEntityIdConnector]

  val testJourneyConfig: MinorEntityIdJourneyConfig = MinorEntityIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out",
    accessibilityUrl = "/accessibility-url"
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

  val testTrust: MinorEntity = MinorEntity(
    None,
    Some(testSautr),
    None,
    None,
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

  val testUnincorpAssoc: MinorEntity = MinorEntity(
    None,
    Some(testSautr),
    None,
    None,
    Some(testPostCode),
    Some(testChrn),
    Some(testCasc),
    testRegistration,
    BvPass,
    Some(testSafeId),
    identifiersMatch = true
  )

  val testOverseasIdentifier = "1234567890"
  val testOverseasIdentifierCountry = "EE"
  val testOverseasIdentifierDetails = OverseasIdentifierDetails(testOverseasIdentifier, testOverseasIdentifierCountry)

  val testNonUkCompanyResponse: JsObject = Json.obj(
    "ctutr" -> testCrn,
    "overseas" -> Json.obj(
      "taxIdentifier" -> testOverseasIdentifier,
      "country" -> testOverseasIdentifierCountry
    ),
    "businessVerification" -> Json.obj(
      "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
    ),
    "registration" -> Json.obj(
      "registrationStatus" -> testRegistration,
      "registeredBusinessPartnerId" -> testSafeId
    ),
    "identifiersMatch" -> true
  )

  val testNonUkCompany: MinorEntity = MinorEntity(
    None,
    None,
    Some(testCrn),
    Some(testOverseasIdentifierDetails),
    None,
    None,
    None,
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

      "return the journey ID when the response JSON includes the journeyId for a Non UK Company" in {
        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        stubPost(createNonUkCompanyJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.createJourney(testJourneyConfig, NonUkNonEstablished))

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
          await(connector.createJourney(testJourneyConfig, UnincorpAssoc))
        }
      }
    }
  }

  "getDetails" must {
    "return trust when Minor Entity Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testTrustJourneyId), OK, Json.stringify(testTrustResponse))
      val res: MinorEntity = await(connector.getDetails(testTrustJourneyId))

      res mustBe testTrust
    }

    "return unincorporated association when Minor Entity Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testUnincorpAssocJourneyId), OK, Json.stringify(testUnincorpAssocResponse))
      val res: MinorEntity = await(connector.getDetails(testUnincorpAssocJourneyId))

      res mustBe testUnincorpAssoc
    }

    "return Non UK Company when Minor Entity Id returns OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubGet(retrieveDetailsUrl(testNonUkCompanyJourneyId), OK, Json.stringify(testNonUkCompanyResponse))
      val res: MinorEntity = await(connector.getDetails(testNonUkCompanyJourneyId))

      res mustBe testNonUkCompany
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
