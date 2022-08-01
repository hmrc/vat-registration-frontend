/*
 * Copyright 2022 HM Revenue & Customs
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
import fixtures.VatRegistrationFixture
import models._
import models.api._
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http._

import java.time.LocalDate
import scala.concurrent.Future

class VatRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  lazy val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  class Setup {
    val connector: VatRegistrationConnector = new VatRegistrationConnector(
      mockHttpClient,
      frontendAppConfig
    ) {
      override lazy val vatRegUrl: String = "tst-url"
      override lazy val vatRegElUrl: String = "test-url"
    }
  }

  val testUrl = "testUrl"

  "Calling createNewRegistration" should {
    "return a successful outcome when the microservice successfully creates a new Vat Registration" in new Setup {
      mockHttpPOSTEmpty[VatScheme]("tst-url", emptyVatScheme)
      connector.createNewRegistration returns emptyVatScheme
    }
    "return a Bad Request response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", badRequest)
      connector.createNewRegistration failedWith badRequest
    }
    "return a Forbidden response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", forbidden)
      connector.createNewRegistration failedWith forbidden
    }
    "return an Upstream4xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream4xx)
      connector.createNewRegistration failedWith upstream4xx
    }
    "return Upstream5xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream5xx)
      connector.createNewRegistration failedWith upstream5xx
    }
    "return a Internal Server Error" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", internalServiceException)
      connector.createNewRegistration failedWith internalServiceException
    }
  }

  "Calling getRegistration" should {
    "return the correct VatResponse when the microservice returns a Vat Registration model" in new Setup {
      mockHttpGET[VatScheme]("tst-url", validVatScheme)
      connector.getRegistration("tstID") returns validVatScheme
    }
    "return the correct VatResponse when the microservice returns a Vat Registration model with a created date" in new Setup {
      mockHttpGET[VatScheme]("tst-url", validVatScheme)
      connector.getRegistration("tstID") returns validVatScheme
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("tst-url", forbidden)
      connector.getRegistration("tstID") failedWith forbidden
    }
    "return the correct VatResponse when a Not Found response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("tst-url", notFound)
      connector.getRegistration("not_found_tstID") failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[VatScheme]("test-url", internalServiceException)
      connector.getRegistration("tstID") failedWith internalServiceException
    }
  }

  "Calling upsertRegistration" should {
    "store a valid VatScheme and return the updated scheme" in new Setup {
      mockHttpPUT[VatScheme, VatScheme]("tst-url", validVatScheme)
      connector.upsertRegistration("tstID", validVatScheme) returns validVatScheme
    }
    "return NOT_FOUND if the registration doesn't exist" in new Setup {
      mockHttpFailedPUT[VatScheme, VatScheme]("tst-url", notFound)
      connector.upsertRegistration("not_found_tstID", validVatScheme) failedWith notFound
    }
  }

  "Calling getRegistrationJson" should {
    "return the correct VatResponse when the microservice returns a Vat Registration model" in new Setup {
      mockHttpGET[JsValue]("tst-url", Json.toJson(validVatScheme))
      connector.getRegistrationJson("tstID") returns Json.toJson(validVatScheme)
    }
    "return the correct VatResponse when the microservice returns a Vat Registration model with a created date" in new Setup {
      mockHttpGET[JsValue]("tst-url", Json.toJson(validVatScheme))
      connector.getRegistrationJson("tstID") returns Json.toJson(validVatScheme)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[JsValue]("tst-url", forbidden)
      connector.getRegistrationJson("tstID") failedWith forbidden
    }
    "return the correct VatResponse when a Not Found response is returned by the microservice" in new Setup {
      mockHttpFailedGET[JsValue]("tst-url", notFound)
      connector.getRegistrationJson("not_found_tstID") failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[JsValue]("test-url", internalServiceException)
      connector.getRegistrationJson("tstID") failedWith internalServiceException
    }
  }

  "Calling upsertPpob" should {

    "return the correct VatResponse when the microservice completes and returns a upsertPpob model" in new Setup {
      mockHttpPATCH[Address, Address]("tst-url", testAddress)
      connector.upsertPpob("tstID", testAddress) returns testAddress
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Address, Address]("tst-url", forbidden)
      connector.upsertPpob("tstID", testAddress) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[Address, Address]("tst-url", notFound)
      connector.upsertPpob("tstID", testAddress) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Address, Address]("tst-url", internalServiceException)
      connector.upsertPpob("tstID", testAddress) failedWith internalServiceException
    }
  }

  "calling submitRegistration" should {
    "return a Success" in new Setup {
      mockHttpPUT[String, HttpResponse]("test-url", validHttpResponse)

      await(connector.submitRegistration("tstID", Map.empty)) mustBe Success
    }
    "return a SubmissionFailed" in new Setup {
      when(mockHttpClient.PUT[String, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("")))

      await(connector.submitRegistration("tstID", Map.empty)) mustBe SubmissionFailed
    }
    "return a SubmissionFailedRetryable" in new Setup {
      when(mockHttpClient.PUT[String, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.failed(new Upstream5xxResponse("502", 502, 502)))

      await(connector.submitRegistration("tstID", Map.empty)) mustBe SubmissionFailedRetryable
    }
  }

  "Calling getBankAccount" should {
    "return the correct response when the microservice completes and returns a BankAccount model" in new Setup {
      mockHttpGET[BankAccount]("tst-url", ukBankAccount)
      connector.getBankAccount("tstID") returns Some(ukBankAccount)
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", forbidden)
      connector.getBankAccount("tstID") failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", notFound)
      connector.getBankAccount("tstID") returns None
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", internalServiceException)
      connector.getBankAccount("tstID") failedWith internalServiceException
    }
  }

  "Calling patchBankAccount" should {
    "return the correct response when the microservice completes and returns a BankAccount model" in new Setup {
      mockHttpPATCH[BankAccount, BankAccount]("tst-url", ukBankAccount)
      connector.patchBankAccount("tstID", ukBankAccount) returns ukBankAccount
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", forbidden)
      connector.patchBankAccount("tstID", ukBankAccount) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", notFound)
      connector.patchBankAccount("tstID", ukBankAccount) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", internalServiceException)
      connector.patchBankAccount("tstID", ukBankAccount) failedWith internalServiceException
    }
  }
}
