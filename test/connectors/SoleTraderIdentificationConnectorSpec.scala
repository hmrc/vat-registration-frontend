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

import config.FrontendAppConfig
import models.api.NETP
import models.external.soletraderid.{OverseasIdentifierDetails, SoleTraderIdJourneyConfig}
import models.external.{BusinessVerificationStatus, BvPass}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException, UnauthorizedException}

class SoleTraderIdentificationConnectorSpec extends VatRegSpec {

  class Setup {
    val config = new FrontendAppConfig(mockServicesConfig, runModeConfiguration = Configuration())
    val connector = new SoleTraderIdentificationConnector(mockHttpClient, config)
    val testJourneyId = "1"
    val testJourneyUrl = "/test-journey-url"
    val createSoleTraderJourneyUrl = "/sole-trader-identification/api/sole-trader-journey"
    val createIndividualJourneyUrl = "/sole-trader-identification/api/individual-journey"
    val retrieveDetailsUrl = s"/sole-trader-identification/api/journey/$testJourneyId"

    val testJourneyConfig = SoleTraderIdJourneyConfig(
      continueUrl = "/test-url",
      optServiceName = Some("MTD"),
      deskProServiceId = "MTDSUR",
      signOutUrl = "/test-sign-out",
      accessibilityUrl = "/accessibility-url",
      regime = "VATC"
    )
  }

  "startSoleTraderJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in new Setup {
        mockHttpPOST(createSoleTraderJourneyUrl, HttpResponse(CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString))

        val res = await(connector.startSoleTraderJourney(testJourneyConfig, NETP))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in new Setup {
        mockHttpPOST(createSoleTraderJourneyUrl, HttpResponse(CREATED, Json.obj().toString))

        intercept[InternalServerException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, NETP))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        mockHttpPOST(createSoleTraderJourneyUrl, HttpResponse(UNAUTHORIZED, ""))

        intercept[UnauthorizedException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, NETP))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createSoleTraderJourneyUrl, HttpResponse(IM_A_TEAPOT, ""))

        intercept[InternalServerException] {
          await(connector.startSoleTraderJourney(testJourneyConfig, NETP))
        }
      }
    }
  }

  "retrieveSoleTraderDetails" when {
    "overseas details are not returned" must {
      "return sole trader details" in new Setup {
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

        mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, testSTIResponse.toString()))
        val res = await(connector.retrieveSoleTraderDetails(testJourneyId))
        res mustBe(testPersonalDetails, testSoleTrader)
      }
    }
    "overseas details are returned" must {
      "return sole trader details when all overseas details are provided" in new Setup {
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
          "overseas" -> Json.obj(
            "taxIdentifier" -> "1234",
            "country" -> "ES"
          ),
          "registration" -> Json.obj(
            "registrationStatus" -> testRegistration,
            "registeredBusinessPartnerId" -> testSafeId
          )
        )

        mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, testSTIResponse.toString()))
        val res = await(connector.retrieveSoleTraderDetails(testJourneyId))
        res mustBe(testPersonalDetails, testSoleTrader.copy(overseas = Some(OverseasIdentifierDetails("1234", "ES"))))
      }
    }
    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidTransactorJson = {
        Json.toJson(testPersonalDetails).as[JsObject] - "firstName"
      }
      mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, Some(Json.obj("personalDetails" -> invalidTransactorJson))))

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
    "throw an InternalServerException for any other status" in new Setup {
      mockHttpGET(retrieveDetailsUrl, HttpResponse(IM_A_TEAPOT, ""))

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
  }

  "startIndividualJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in new Setup {
        mockHttpPOST(createIndividualJourneyUrl, HttpResponse(CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString))

        val res = await(connector.startIndividualJourney(testJourneyConfig))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in new Setup {
        mockHttpPOST(createIndividualJourneyUrl, HttpResponse(CREATED, Json.obj().toString))

        intercept[InternalServerException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        mockHttpPOST(createIndividualJourneyUrl, HttpResponse(UNAUTHORIZED, ""))

        intercept[UnauthorizedException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createIndividualJourneyUrl, HttpResponse(IM_A_TEAPOT, ""))

        intercept[InternalServerException] {
          await(connector.startIndividualJourney(testJourneyConfig))
        }
      }
    }
  }

  "retrieveIndividualDetails" when {
    "overseas details are not returned" must {
      "return transactor details" in new Setup {
        val testSTIResponse: JsObject = Json.obj(
          "fullName" -> Json.obj(
            "firstName" -> testFirstName,
            "lastName" -> testLastName
          ),
          "nino" -> testApplicantNino,
          "dateOfBirth" -> testApplicantDob
        )

        mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, testSTIResponse.toString()))
        val res = await(connector.retrieveIndividualDetails(testJourneyId))
        res mustBe testPersonalDetails
      }
    }
    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidTransactorJson = {
        Json.toJson(testPersonalDetails).as[JsObject] - "firstName"
      }
      mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, Some(Json.obj("personalDetails" -> invalidTransactorJson))))

      intercept[InternalServerException] {
        await(connector.retrieveIndividualDetails(testJourneyId))
      }
    }
    "throw an InternalServerException for any other status" in new Setup {
      mockHttpGET(retrieveDetailsUrl, HttpResponse(IM_A_TEAPOT, ""))

      intercept[InternalServerException] {
        await(connector.retrieveIndividualDetails(testJourneyId))
      }
    }
  }
}