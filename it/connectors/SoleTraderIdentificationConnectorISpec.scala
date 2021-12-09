/*
 * Copyright 2021 HM Revenue & Customs
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
import models.PersonalDetails
import models.api.Individual
import models.external.{BusinessVerificationStatus, BvPass, SoleTraderIdEntity}
import models.external.soletraderid.{OverseasIdentifierDetails, SoleTraderIdJourneyConfig}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{CREATED, IM_A_TEAPOT, OK, UNAUTHORIZED, _}
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, UnauthorizedException}

class SoleTraderIdentificationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val testJourneyId = "1"
  val testJourneyUrl = "/test-journey-url"
  val createSoleTraderJourneyUrl = "/sole-trader-identification/api/sole-trader-journey"
  val createIndividualJourneyUrl = "/sole-trader-identification/api/individual-journey"
  val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"
  val connector: SoleTraderIdentificationConnector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  val testJourneyConfig: SoleTraderIdJourneyConfig = SoleTraderIdJourneyConfig(
    continueUrl = "/test-url",
    optServiceName = Some("MTD"),
    deskProServiceId = "MTDSUR",
    signOutUrl = "/test-sign-out",
    accessibilityUrl = "/accessibility-url",
    regime = "VATC",
    businessVerificationCheck = true
  )

  "startSoleTraderJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in {
        given()
        stubPost(createSoleTraderJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.startSoleTraderJourney(testJourneyConfig, Individual))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in {
        given()
        stubPost(createSoleTraderJourneyUrl, CREATED, "{}")

        intercept[InternalServerException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, Individual))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        given()
        stubPost(createSoleTraderJourneyUrl, UNAUTHORIZED, "")

        intercept[UnauthorizedException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, Individual))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        given()
        stubPost(createSoleTraderJourneyUrl, IM_A_TEAPOT, "")

        intercept[InternalServerException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, Individual))
        }
      }
    }
  }

  "startIndividualJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in {
        given()
        stubPost(createIndividualJourneyUrl, CREATED, Json.stringify(Json.obj("journeyStartUrl" -> testJourneyUrl)))

        val res = await(connector.startIndividualJourney(testJourneyConfig))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in {
        given()
        stubPost(createIndividualJourneyUrl, CREATED, "{}")

        intercept[InternalServerException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        given()
        stubPost(createIndividualJourneyUrl, UNAUTHORIZED, "")

        intercept[UnauthorizedException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        given()
        stubPost(createIndividualJourneyUrl, IM_A_TEAPOT, "")

        intercept[InternalServerException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "return transactor details when STI returns OK" in new Setup {
      given()

      val testSTIResponse: JsObject = Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        ),
        "nino" -> testApplicantNino,
        "dateOfBirth" -> testApplicantDob,
        "sautr" -> testSautr,
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        ),
        "registration" -> Json.obj(
          "registrationStatus" -> testRegistration,
          "registeredBusinessPartnerId" -> testSafeId
        )
      )

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testSTIResponse))
      val res: (PersonalDetails, SoleTraderIdEntity) = await(connector.retrieveSoleTraderDetails(testJourneyId))

      res mustBe(testPersonalDetails, testSoleTrader)
    }

    "return transactor details for NETP when STI returns OK" in new Setup {
      given()

      val testSTIResponse: JsObject = Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        ),
        "dateOfBirth" -> testApplicantDob,
        "sautr" -> testSautr,
        "trn" -> testTrn,
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        ),
        "registration" -> Json.obj(
          "registrationStatus" -> testRegistration,
          "registeredBusinessPartnerId" -> testSafeId
        ),
        "identifiersMatch" -> false
      )

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testSTIResponse))
      val res: (PersonalDetails, SoleTraderIdEntity) = await(connector.retrieveSoleTraderDetails(testJourneyId))

      res mustBe(testNetpPersonalDetails, testNetpSoleTrader)
    }

    "return transactor details for NETP when an overseas identifier is returned" in new Setup {
      given()

      val testSTIResponse: JsObject = Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        ),
        "dateOfBirth" -> testApplicantDob,
        "sautr" -> testSautr,
        "trn" -> testTrn,
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        ),
        "registration" -> Json.obj(
          "registrationStatus" -> testRegistration,
          "registeredBusinessPartnerId" -> testSafeId
        ),
        "overseas" -> Json.obj(
          "taxIdentifier" -> "123456789",
          "country" -> "FR"
        ),
        "identifiersMatch" -> false
      )

      stubGet(retrieveDetailsUrl, OK, Json.stringify(testSTIResponse))
      val res: (PersonalDetails, SoleTraderIdEntity) = await(connector.retrieveSoleTraderDetails(testJourneyId))

      res mustBe(testNetpPersonalDetails, testNetpSoleTrader.copy(overseas = Some(OverseasIdentifierDetails("123456789", "FR"))))
    }

    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      given()

      val invalidTransactorJson: JsObject = {
        Json.toJson(testPersonalDetails).as[JsObject] - "firstName"
      }
      stubGet(retrieveDetailsUrl, OK, Json.stringify(Json.obj("personalDetails" -> invalidTransactorJson)))

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }

    "throw an InternalServerException for any other status" in new Setup {
      given()

      stubGet(retrieveDetailsUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
  }

}
