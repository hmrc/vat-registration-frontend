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

import enums.DownstreamOutcome
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{InternalServerException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse, _}

import scala.concurrent.ExecutionContext.Implicits.global

class VatRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new VatRegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  implicit val hc = HeaderCarrier()

  "Calling createNewRegistration" should {
    "return a successful outcome when the microservice successfully creates a new Vat Registration" in new Setup {
      mockHttpPOSTEmpty[VatScheme]("tst-url", validNewVatScheme)
      ScalaFutures.whenReady(connector.createNewRegistration())(_ mustBe validNewVatScheme)
    }
    "return a Bad Request response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", badRequest)
      ScalaFutures.whenReady(connector.createNewRegistration().failed)(_ mustBe badRequest)
    }
    "return a Forbidden response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.createNewRegistration().failed)(_ mustBe forbidden)
    }
    "return an Upstream4xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream4xx)
      ScalaFutures.whenReady(connector.createNewRegistration().failed)(_ mustBe upstream4xx)
    }
    "return Upstream5xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream5xx)
      ScalaFutures.whenReady(connector.createNewRegistration().failed)(_ mustBe upstream5xx)
    }
    "return a Internal Server Error" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", internalServiceException)
      ScalaFutures.whenReady(connector.createNewRegistration().failed)(_ mustBe internalServiceException)
    }
  }

  "Calling getRegistration" should {
    "return the correct VatResponse when the microservice returns a Vat Registration model" in new Setup {
      mockHttpGET[VatScheme]("tst-url", validVatScheme)
      ScalaFutures.whenReady(connector.getRegistration("tstID"))(_ mustBe validVatScheme)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.getRegistration("tstID").failed)(_ mustBe forbidden)
    }
    "return the correct VatResponse when a Not Found response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("tst-url", notFound)
      ScalaFutures.whenReady(connector.getRegistration("not_found_tstID").failed)(_ mustBe notFound)
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("test-url", internalServiceException)
      ScalaFutures.whenReady(connector.getRegistration("tstID").failed)(_ mustBe internalServiceException)
    }
  }

  "Calling upsertVatChoice" should {
    "return the correct VatResponse when the microservice completes and returns a VatChoice model" in new Setup {
      mockHttpPATCH[VatChoice, VatChoice]("tst-url", validVatChoice)
      ScalaFutures.whenReady(connector.upsertVatChoice("tstID", validVatChoice))(_ mustBe validVatChoice)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.upsertVatChoice("tstID", validVatChoice).failed)(_ mustBe forbidden)
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", notFound)
      ScalaFutures.whenReady(connector.upsertVatChoice("tstID", validVatChoice).failed)(_ mustBe notFound)
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", internalServiceException)
      ScalaFutures.whenReady(connector.upsertVatChoice("tstID", validVatChoice).failed)(_ mustBe internalServiceException)
    }
  }

  "Calling upsertVatTradingDetails" should {
    "return the correct VatResponse when the microservice completes and returns a VatTradingDetails model" in new Setup {
      mockHttpPATCH[VatTradingDetails, VatTradingDetails]("tst-url", validVatTradingDetails)
      ScalaFutures.whenReady(connector.upsertVatTradingDetails("tstID", validVatTradingDetails))(_ mustBe validVatTradingDetails)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.upsertVatTradingDetails("tstID", validVatTradingDetails).failed)(_ mustBe forbidden)
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", notFound)
      ScalaFutures.whenReady(connector.upsertVatTradingDetails("tstID", validVatTradingDetails).failed)(_ mustBe notFound)
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatTradingDetails, VatTradingDetails]("tst-url", internalServiceException)
      ScalaFutures.whenReady(connector.upsertVatTradingDetails("tstID", validVatTradingDetails).failed)(_ mustBe internalServiceException)
    }
  }
}
