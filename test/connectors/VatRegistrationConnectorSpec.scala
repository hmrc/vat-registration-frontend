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
import models.api.returns.Returns
import models.external.{EmailAddress, EmailVerified}
import models.view.{_}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http._

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
      mockHttpGET[VatScheme]("tst-url", vatSchemeWithDate)
      connector.getRegistration("tstID") returns vatSchemeWithDate
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
      connector.upsertRegistration( "tstID", validVatScheme) returns validVatScheme
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
      mockHttpGET[JsValue]("tst-url", Json.toJson(vatSchemeWithDate))
      connector.getRegistrationJson("tstID") returns Json.toJson(vatSchemeWithDate)
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

  "Calling getAckRef" should {
    "return a Acknowldegement Reference when it can be retrieved from the microservice" in new Setup {
      mockHttpGET[String]("tst-url", "Fake Ref No")
      await(connector.getAckRef("tstID")) mustBe "Fake Ref No"
    }

    "fail when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Option[String]]("tst-url", forbidden)
      connector.getAckRef("tstID") failedWith forbidden
    }
    "fail when a Not Found response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Option[String]]("tst-url", notFound)
      connector.getAckRef("not_found_tstID") failedWith notFound
    }
    "fail when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Option[String]]("test-url", internalServiceException)
      connector.getAckRef("tstID") failedWith internalServiceException
    }
  }

  "Calling getTaxableThreshold" must {
    "return a turnover threshold model" in new Setup {
      val taxableThreshold = TaxableThreshold("100000", "2018-01-01")
      mockHttpGET[TaxableThreshold]("tst-url", taxableThreshold)
      connector.getTaxableThreshold(date) returns taxableThreshold
    }

    "fail when an exception is returned" in new Setup {
      mockHttpFailedGET("tst-url", exception)
      connector.getTaxableThreshold(date) failedWith exception
    }
  }

  "Calling deleteVatScheme" should {
    val notFoundException = new Exception(NOT_FOUND.toString)

    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[HttpResponse]("tst-url", HttpResponse(OK, ""))
      connector.deleteVatScheme("regId")
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", notFoundException)
      connector.deleteVatScheme("regId") failedWith notFoundException
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

  "Calling getSicAndCompliance" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "businessDescription": "Test Description",
         |  "labourCompliance": {
         |    "numOfWorkersSupplied": 8,
         |    "intermediaryArrangement": true
         |  },
         |  "mainBusinessActivity": {
         |    "id": "testId",
         |    "description": "test Desc",
         |    "displayDetails": "test Details"
         |  }
         |}""".stripMargin)
    val httpRespOK = HttpResponse(OK, validJson.toString())
    val httpRespNOCONTENT = HttpResponse(NO_CONTENT, "")

    "return a JsValue" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespOK)
      connector.getSicAndCompliance returns Some(validJson)
    }

    "return None if there is no data for the registration" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespNOCONTENT)
      connector.getSicAndCompliance returns None
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getSicAndCompliance failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getSicAndCompliance failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServerError)
      connector.getSicAndCompliance failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", exception)
      connector.getSicAndCompliance failedWith exception
    }
  }

  "Calling updateSicAndCompliance" should {
    val sicAndCompliance = SicAndCompliance(
      description = Some(BusinessActivityDescription("test Bus Desc")),
      mainBusinessActivity = Some(MainBusinessActivityView(SicCode("testId", "test Desc", "test Details"))),
      businessActivities = Some(BusinessActivities(List(SicCode("99889", "otherBusiness", "otherBusiness1")))),
      supplyWorkers = Some(SupplyWorkers(true)),
      workers = Some(Workers(8)),
      intermediarySupply = Some(IntermediarySupply(true))
    )

    val json = Json.parse(
      s"""
         |{
         |  "businessDescription": "test Bus Desc",
         |  "labourCompliance": {
         |    "numOfWorkersSupplied": 8,
         |    "intermediaryArrangement": true
         |  },
         |"businessActivities": [
         |  {
         |     "id": "99889",
         |     "description": "otherBusiness",
         |     "displayDetails": "otherBusiness1"
         |  }
         |  ],
         |  "mainBusinessActivity": {
         |    "id": "testId",
         |    "description": "test Desc",
         |    "displayDetails": "test Details"
         |  }
         |}""".stripMargin)

    "return a JsValue with a Sic and Compliance view model" in new Setup {
      mockHttpPATCH[JsValue, JsValue]("tst-url", json)
      connector.updateSicAndCompliance(sicAndCompliance) returns json
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", notFound)
      connector.updateSicAndCompliance(sicAndCompliance) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", forbidden)
      connector.updateSicAndCompliance(sicAndCompliance) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", internalServerError)
      connector.updateSicAndCompliance(sicAndCompliance) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", exception)
      connector.updateSicAndCompliance(sicAndCompliance) failedWith exception
    }
  }

  "Calling getReturns" should {
    "return the correct response when the microservice completes and returns a Returns model" in new Setup {
      mockHttpGET[Returns]("tst-url", returns)
      connector.getReturns("tstID") returns returns
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", forbidden)
      connector.getReturns("tstID") failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", notFound)
      connector.getReturns("tstID") failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", internalServiceException)
      connector.getReturns("tstID") failedWith internalServiceException
    }
  }

  "Calling patchReturns" should {
    "return the correct response when the microservice completes and returns a Returns model" in new Setup {
      mockHttpPATCH[Returns, Returns]("tst-url", returns)
      connector.patchReturns("tstID", returns) returns returns
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", forbidden)
      connector.patchReturns("tstID", returns) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", notFound)
      connector.patchReturns("tstID", returns) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", internalServiceException)
      connector.patchReturns("tstID", returns) failedWith internalServiceException
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

  "getEligibilityData" should {
    "return 200 and a JsObject" in new Setup {
      val json = Json.obj("foo" -> "bar")
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(200, json.toString()))
      connector.getEligibilityData returns json
    }

    "return 404 (no 2xx code) and an execption should be thrown" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getEligibilityData failedWith notFound
    }
  }
}
