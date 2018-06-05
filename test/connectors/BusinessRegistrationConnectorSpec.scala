/*
 * Copyright 2018 HM Revenue & Customs
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

import config.WSHttp
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpResponse, InternalServerException, NotFoundException}

class BusinessRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {
  class Setup() {
    val connector = new BusinessRegistrationConnector {
      override val businessRegistrationUrl: String = "tst-url"
      override val businessRegistrationUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  val regId = "testRegId"
  val brResponseJson =
    s"""
      |{
      |   "registrationID" : "$regId"
      |}
    """.stripMargin

  "Calling getBusinessRegistrationID" should {
    "retrieve a registration ID" when {
      "there is a BR document" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(OK, Some(Json.parse(brResponseJson))))

        await(connector.getBusinessRegistrationID) mustBe Some(regId)
      }
    }

    "retrieve no registration ID" when {
      "there is not a BR document" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(NOT_FOUND))

        await(connector.getBusinessRegistrationID) mustBe None
      }

      "the BR document does not have a regId" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(OK, Some(Json.parse("{}"))))

        await(connector.getBusinessRegistrationID) mustBe None
      }
    }
  }

  "getTradingName" should {
    "return a trading name" when {
      "there is one in business registration" in new Setup {
        mockHttpGET[JsValue]("tst-urltst-url", Json.parse("""{"tradingName": "Foo Bar"}"""))

        await(connector.retrieveTradingName("someRegId")) mustBe Some("Foo Bar")
      }
    }
    "return no trading name" when {
      "there is none in business registration" in new Setup {
        mockHttpGET[JsValue]("tst-urltst-url", Json.obj())

        await(connector.retrieveTradingName("someRegId")) mustBe None
      }

      "there an invalid trading name" in new Setup {
        mockHttpGET[JsValue]("tst-urltst-url", Json.parse("""{"tradingNameTroup": "Foo Bar"}"""))

        await(connector.retrieveTradingName("someRegId")) mustBe None
      }
      "a not found exception is experienced" in new Setup {
        mockHttpFailedGET("tst-urltst-url", new NotFoundException(""))

        await(connector.retrieveTradingName("someRegId")) mustBe None
      }
      "there is an error in business registration" in new Setup {
        mockHttpFailedGET("tst-urltst-url", new InternalServerException(""))

        await(connector.retrieveTradingName("someRegId")) mustBe None
      }
    }
  }

  "upsertTradingName" should {
    "return the passed in trading name" when {
      "the upsert has been successful" in new Setup {
        mockHttpPOST[JsValue, HttpResponse]("tst-urltst-url",HttpResponse(200))

        await(connector.upsertTradingName("someRegId", "New Foo Bar")) mustBe "New Foo Bar"
      }

      "an error occured in br" in new Setup {
        mockHttpFailedPOST[JsValue, HttpResponse]("tst-urltst-url", new InternalServerException(""))

        await(connector.upsertTradingName("someRegId", "New Foo Bar")) mustBe "New Foo Bar"
      }
    }
  }

}