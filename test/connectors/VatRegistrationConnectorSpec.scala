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
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

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
      mockHttpPOSTEmpty[VatScheme]("tst-url", emptyVatScheme)
      ScalaFutures.whenReady(connector.createNewRegistration())(_ mustBe emptyVatScheme)
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

  "Calling deleteVatScheme" should {
    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[Boolean]("tst-url", true)
      ScalaFutures.whenReady(connector.deleteVatScheme("regId"))(_ mustBe true)
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[Boolean]("tst-url", notFound)
      ScalaFutures.whenReady(connector.deleteVatScheme("regId").failed)(_ mustBe notFound)
    }
  }

  "Calling deleteAccountingPeriodStart" should {
    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[Boolean]("tst-url", true)
      ScalaFutures.whenReady(connector.deleteAccountingPeriodStart("regId"))(_ mustBe true)
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[Boolean]("tst-url", notFound)
      ScalaFutures.whenReady(connector.deleteAccountingPeriodStart("regId").failed)(_ mustBe notFound)
    }
  }

  "Calling deleteBankAccount" should {
    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[Boolean]("tst-url", true)
      ScalaFutures.whenReady(connector.deleteBankAccount("regId"))(_ mustBe true)
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[Boolean]("tst-url", notFound)
      ScalaFutures.whenReady(connector.deleteBankAccount("regId").failed)(_ mustBe notFound)
    }
  }

  "Calling deleteZeroRatedTurnover" should {
    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[Boolean]("tst-url", true)
      ScalaFutures.whenReady(connector.deleteZeroRatedTurnover("regId"))(_ mustBe true)
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[Boolean]("tst-url", notFound)
      ScalaFutures.whenReady(connector.deleteZeroRatedTurnover("regId").failed)(_ mustBe notFound)
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

  "Calling upsertVatFinancials" should {
    "return the correct VatResponse when the microservice completes and returns a VatFinancials model" in new Setup {
      mockHttpPATCH[VatFinancials, VatFinancials]("tst-url", VatFinancials.empty)
      ScalaFutures.whenReady(connector.upsertVatFinancials("tstID", VatFinancials.empty))(_ mustBe VatFinancials.empty)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.upsertVatFinancials("tstID", VatFinancials.empty).failed)(_ mustBe forbidden)
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", notFound)
      ScalaFutures.whenReady(connector.upsertVatFinancials("tstID", VatFinancials.empty).failed)(_ mustBe notFound)
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", internalServiceException)
      ScalaFutures.whenReady(connector.upsertVatFinancials("tstID", VatFinancials.empty).failed)(_ mustBe internalServiceException)
    }
  }

  "Calling upsertSicAndCompliance" should {
    "return the correct VatResponse when the microservice completes and returns a SicAndCompliance model" in new Setup {
      mockHttpPATCH[VatFinancials, VatSicAndCompliance]("tst-url", VatSicAndCompliance.empty)
      ScalaFutures.whenReady(connector.upsertSicAndCompliance("tstID", VatSicAndCompliance.empty))(_ mustBe VatSicAndCompliance.empty)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", forbidden)
      ScalaFutures.whenReady(connector.upsertSicAndCompliance("tstID", VatSicAndCompliance.empty).failed)(_ mustBe forbidden)
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", notFound)
      ScalaFutures.whenReady(connector.upsertSicAndCompliance("tstID", VatSicAndCompliance.empty).failed)(_ mustBe notFound)
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", internalServiceException)
      ScalaFutures.whenReady(connector.upsertSicAndCompliance("tstID", VatSicAndCompliance.empty).failed)(_ mustBe internalServiceException)
    }
  }
}
