/*
 * Copyright 2017 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import config.WSHttp
import features.tradingDetails.models.TradingDetails

import scala.language.postfixOps

class TradingDetailsConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new RegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val vatRegElUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "Calling upsertVatTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      mockHttpPATCH[VatTradingDetails, VatTradingDetails]("tst-url", validVatTradingDetails)
      connector.upsertVatTradingDetails("tstID", validVatTradingDetails) returns validVatTradingDetails
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", forbidden)
      connector.upsertVatTradingDetails("tstID", validVatTradingDetails) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", notFound)
      connector.upsertVatTradingDetails("tstID", validVatTradingDetails) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", internalServiceException)
      connector.upsertVatTradingDetails("tstID", validVatTradingDetails) failedWith internalServiceException
    }
  }


  "Calling upsertTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      mockHttpPATCH[TradingDetails, TradingDetails]("tst-url", validTradingDetails)
      connector.upsertTradingDetails("tstID", validTradingDetails) returns validTradingDetails
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TradingDetails, TradingDetails]("tst-url", forbidden)
      connector.upsertTradingDetails("tstID", validTradingDetails) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[TradingDetails, TradingDetails]("tst-url", notFound)
      connector.upsertTradingDetails("tstID", validTradingDetails) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TradingDetails, TradingDetails]("tst-url", internalServiceException)
      connector.upsertTradingDetails("tstID", validTradingDetails) failedWith internalServiceException
    }
  }

  "Calling getTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      mockHttpGET[TradingDetails]("tst-url", validTradingDetails)
      connector.getTradingDetails("tstID") returns Some(validTradingDetails)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[TradingDetails]("tst-url", forbidden)
      connector.getTradingDetails("tstID") failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[TradingDetails]("tst-url", notFound)
      connector.getTradingDetails("tstID") returns None
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[TradingDetails]("tst-url", internalServiceException)
      connector.getTradingDetails("tstID") failedWith internalServiceException
    }
  }
}