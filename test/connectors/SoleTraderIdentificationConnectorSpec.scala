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
    implicit val ex = scala.concurrent.ExecutionContext.Implicits.global
    val config = new FrontendAppConfig(mockServicesConfig, runModeConfiguration = Configuration())
    val connector = new SoleTraderIdentificationConnector(mockHttpClient, config)(ex)
    val testJourneyId = "1"
    val testJourneyUrl = "/test-journey-url"
    val createJourneyUrl = "/sole-trader-identification/journey"
    val retrieveDetailsUrl = s"/sole-trader-identification/journey/$testJourneyId"

    val testJourneyConfig = SoleTraderIdJourneyConfig(
      continueUrl = "/test-url",
      optServiceName = Some("MTD"),
      deskProServiceId = "MTDSUR",
      signOutUrl = "/test-sign-out",
      accessibilityUrl = "/accessibility-url",
      enableSautrCheck = false
    )
  }

  "startJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString))

        val res = await(connector.startJourney(testJourneyConfig, NETP))

        res mustBe testJourneyUrl
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(CREATED, Json.obj().toString))

        intercept[InternalServerException] {
          await(connector.startJourney(testJourneyConfig, NETP))
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an UnauthorizedException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(UNAUTHORIZED, ""))

        intercept[UnauthorizedException] {
          await(connector.startJourney(testJourneyConfig, NETP))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(IM_A_TEAPOT, ""))

        intercept[InternalServerException] {
          await(connector.startJourney(testJourneyConfig, NETP))
        }
      }
    }
  }

  "retrieveSoleTraderDetails" when {
    "overseas details are not returned" must {
      "return transactor details" in new Setup {
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
        res mustBe(testTransactorDetails, testSoleTrader)
      }
    }
    "overseas details are returned" must {
      "return transactor details when all overseas details are provided" in new Setup {
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
        res mustBe(testTransactorDetails, testSoleTrader.copy(overseas = Some(OverseasIdentifierDetails("1234", "ES"))))
      }
    }
    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidTransactorJson = {
        Json.toJson(testTransactorDetails).as[JsObject] - "firstName"
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
}