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

  val badRequest = new BadRequestException(Status.BAD_REQUEST.toString)
  val forbidden = Upstream4xxResponse(Status.FORBIDDEN.toString, Status.FORBIDDEN, Status.FORBIDDEN)
  val upstream4xx = Upstream4xxResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx = Upstream5xxResponse(Status.INTERNAL_SERVER_ERROR.toString, Status.INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR)
  val notFound = new NotFoundException(Status.NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(Status.BAD_GATEWAY.toString)

  "Calling createNewRegistration" should {
    "return a successful outcome when the microservice successfully creates a new Vat Registration" in new Setup {
      mockHttpPOSTEmpty[HttpResponse]("tst-url", HttpResponse(Status.CREATED))
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Success)
    }
    "return a failed outcome when the microservice returns a 2xx response other than CREATED" in new Setup {
      mockHttpPOSTEmpty[HttpResponse]("tst-url", HttpResponse(Status.ACCEPTED))
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
    "return a Bad Request response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", badRequest)
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
    "return a Forbidden response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", forbidden)
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
    "return an Upstream4xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream4xx)
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
    "return Upstream5xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream5xx)
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
    "return a Internal Server Error" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", internalServiceException)
      connector.createNewRegistration().map(_ mustBe DownstreamOutcome.Failure)
    }
  }

  "Calling getRegistration" should {
    "return the correct VatResponse when the microservice returns a Vat Registration model" in new Setup {
      mockHttpGET[VatScheme]("tst-url", validVatScheme)
      connector.getRegistration("tstID").map(_ mustBe validVatScheme)
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
      connector.upsertVatChoice("tstID", validVatChoice).map(_ mustBe validVatChoice)
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
      connector.upsertVatTradingDetails("tstID", validVatTradingDetails).map(_ mustBe validVatTradingDetails)
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
