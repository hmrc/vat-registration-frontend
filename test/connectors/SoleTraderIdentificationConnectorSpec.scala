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
import play.api.{ConfigLoader, Configuration}
import play.api.libs.json.{JsObject, JsValue, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import play.api.test.Helpers._

import scala.concurrent.Future


class SoleTraderIdentificationConnectorSpec extends VatRegSpec {

  class Setup {
    implicit val ex = scala.concurrent.ExecutionContext.Implicits.global
    val config = new FrontendAppConfig(mockServicesConfig, runModeConfiguration = Configuration())
    val connector = new SoleTraderIdentificationConnector(mockHttpClient, config)(ex)
    val testJourneyId = "1"
    val createJourneyUrl = "/sole-trader-identification/journey"
    val retrieveDetailsUrl = s"/sole-trader-identification/journey/$testJourneyId"
  }

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(CREATED, Some(Json.obj("journeyId" -> testJourneyId))))

        val res = await(connector.createJourney)

        res mustBe testJourneyId
      }
      "throw an InternalServerException when the response JSON doesn't contain the journeyId" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(CREATED, Some(Json.obj())))

        intercept[InternalServerException] {
          await(connector.createJourney)
        }
      }
    }
    "the API returns UNAUTHORISED" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(UNAUTHORIZED, None))

        intercept[InternalServerException] {
          await(connector.createJourney)
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(IM_A_TEAPOT, None))

        intercept[InternalServerException] {
          await(connector.createJourney)
        }
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "return transactor details when STI returns OK" in new Setup {
      mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, Some(Json.obj("personalDetails" -> Json.toJson(testTransactorDetails)))))
      val res = await(connector.retrieveSoleTraderDetails(testJourneyId))
      res mustBe testTransactorDetails
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
      mockHttpGET(retrieveDetailsUrl, HttpResponse(IM_A_TEAPOT, None))

      intercept[InternalServerException] {
        await(connector.retrieveSoleTraderDetails(testJourneyId))
      }
    }
  }

}