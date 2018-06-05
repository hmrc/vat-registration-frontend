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
import features.tradingDetails.{TradingDetails, TradingNameView}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.HttpResponse

import scala.language.postfixOps

class TradingDetailsConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new RegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val vatRegElUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  val tradingNameViewNo = TradingNameView(yesNo = false, None)
  val fullS4L = TradingDetails(
    Some(tradingNameViewNo),
    Some(false)
  )

  "Calling upsertTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      val resp = HttpResponse(200)

      mockHttpPATCH[TradingDetails, HttpResponse]("tst-url", resp)
      connector.upsertTradingDetails("tstID", fullS4L) returns resp
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TradingDetails, HttpResponse]("tst-url", forbidden)
      connector.upsertTradingDetails("tstID", fullS4L) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[TradingDetails, HttpResponse]("tst-url", notFound)
      connector.upsertTradingDetails("tstID", fullS4L) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TradingDetails, HttpResponse]("tst-url", internalServiceException)
      connector.upsertTradingDetails("tstID", fullS4L) failedWith internalServiceException
    }
  }

  "Calling getTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      val jsobj = Json.obj(
        "eoriRequested" -> JsBoolean(false)
      )
      val resp = HttpResponse(200, Some(jsobj))

      mockHttpGET[HttpResponse]("tst-url", resp)
      connector.getTradingDetails("tstID") returns Some(fullS4L)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getTradingDetails("tstID") failedWith forbidden
    }
    "return a Not Found S4LTradingDetails when the microservice returns a NoContent response (No VatRegistration in database)" in new Setup {
      val resp = HttpResponse(204)

      mockHttpGET[HttpResponse]("tst-url", resp)
      connector.getTradingDetails("tstID") returns None
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServiceException)
      connector.getTradingDetails("tstID") failedWith internalServiceException
    }
  }
}
