/*
 * Copyright 2020 HM Revenue & Customs
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
import models.external.{EmailAddress, EmailVerified}
import models.view.{ApplicantDetails, _}
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

  "Calling getAckRef" should {
    "return a Acknowldegement Reference when it can be retrieved from the microservice" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(OK, "Fake Ref No"))
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
      mockHttpDELETE[HttpResponse]("tst-url", HttpResponse(OK))
      connector.deleteVatScheme("regId")
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", notFoundException)
      connector.deleteVatScheme("regId") failedWith notFoundException
    }
  }

  "Calling updateBusinessContact" should {

    "return the correct VatResponse when the microservice completes and returns a BusinessContact model" in new Setup {
      mockHttpPATCH[BusinessContact, BusinessContact]("tst-url", validBusinessContactDetails)
      connector.upsertBusinessContact(validBusinessContactDetails) returns validBusinessContactDetails
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH("tst-url", forbidden)
      connector.upsertBusinessContact(validBusinessContactDetails) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH("tst-url", notFound)
      connector.upsertBusinessContact(validBusinessContactDetails) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH("tst-url", internalServiceException)
      connector.upsertBusinessContact(validBusinessContactDetails) failedWith internalServiceException
    }
  }

  "Calling getBusinessContact" should {

    val businessContactJson = Json.parse(
      """
        |{
        | "digitalContact": {
        |   "email": "me@you.com",
        |   "tel": "123456738374",
        |   "mobile": "123456789876"
        | },
        | "website": "www.wwwwwwwwwww.com",
        | "ppob": {
        |   "line1": "test",
        |   "line2": "test",
        |   "postcode": "XX1 1XX",
        |   "country": "United Kingdom"
        | },
        | "contactPreference": "Email"
        |}
      """.stripMargin)

    val expectedModel = BusinessContact(
      ppobAddress = Some(ScrsAddress(
        line1 = "test",
        line2 = "test",
        line3 = None,
        line4 = None,
        postcode = Some("XX1 1XX"),
        country = Some("United Kingdom")
      )),
      companyContactDetails = Some(CompanyContactDetails(
        email = "me@you.com",
        phoneNumber = Some("123456738374"),
        mobileNumber = Some("123456789876"),
        websiteAddress = Some("www.wwwwwwwwwww.com")
      )),
      contactPreference = Some(Email)
    )

    "return the correct Http response when the microservice completes and returns a BusinessContact model" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(200, Some(businessContactJson)))
      connector.getBusinessContact returnsSome expectedModel
    }

    "returns None if a 204 is recieved" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(204, Some(Json.obj())))
      connector.getBusinessContact returnsNone
    }

    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET("tst-url", forbidden)
      connector.getBusinessContact failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET("tst-url", notFound)
      connector.getBusinessContact failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET("tst-url", internalServiceException)
      connector.getBusinessContact failedWith internalServiceException
    }
  }

  "Calling getThreshold" should {

    val vatThreshold = optVoluntaryRegistration

    "return the correct VatResponse when the microservice completes and returns a Threshold model" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(200, Some(Json.toJson(vatThreshold))))
      connector.getThreshold("tstID") returns vatThreshold
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET("tst-url", forbidden)
      connector.getThreshold("tstID") failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET("tst-url", notFound)
      connector.getThreshold("tstID") failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET("tst-url", internalServiceException)
      connector.getThreshold("tstID") failedWith internalServiceException
    }
  }

  "Calling upsertPpob" should {

    "return the correct VatResponse when the microservice completes and returns a upsertPpob model" in new Setup {
      mockHttpPATCH[ScrsAddress, ScrsAddress]("tst-url", scrsAddress)
      connector.upsertPpob("tstID", scrsAddress) returns scrsAddress
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[ScrsAddress, ScrsAddress]("tst-url", forbidden)
      connector.upsertPpob("tstID", scrsAddress) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[ScrsAddress, ScrsAddress]("tst-url", notFound)
      connector.upsertPpob("tstID", scrsAddress) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[ScrsAddress, ScrsAddress]("tst-url", internalServiceException)
      connector.upsertPpob("tstID", scrsAddress) failedWith internalServiceException
    }
  }

  "calling submitRegistration" should {
    "return a Success" in new Setup {
      mockHttpPUT[String, HttpResponse]("test-url", validHttpResponse)

      await(connector.submitRegistration("tstID")) mustBe Success
    }
    "return a SubmissionFailed" in new Setup {
      when(mockHttpClient.PUT[String, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.failed(new Upstream4xxResponse("400", 400, 400)))

      await(connector.submitRegistration("tstID")) mustBe SubmissionFailed
    }
    "return a SubmissionFailedRetryable" in new Setup {
      when(mockHttpClient.PUT[String, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.failed(new Upstream5xxResponse("502", 502, 502)))

      await(connector.submitRegistration("tstID")) mustBe SubmissionFailedRetryable
    }
  }

  "getTurnoverEstimates" should {

    val jsonBody = Json.obj("turnoverEstimate" -> 1000)
    val turnoverEstimates = TurnoverEstimates(1000L)

    "return turnover estimates if they are returned from the backend" in new Setup {
      mockHttpGET[HttpResponse](testUrl, HttpResponse(200, Some(jsonBody)))

      val result: Option[TurnoverEstimates] = await(connector.getTurnoverEstimates)
      result mustBe Some(turnoverEstimates)
    }

    "return None if turnover estimates can't be found in the backend for the supplied regId" in new Setup {
      mockHttpGET[HttpResponse](testUrl, HttpResponse(204))

      val result: Option[TurnoverEstimates] = await(connector.getTurnoverEstimates)
      result mustBe None
    }
  }

  "Calling getApplicantDetails(testRegId)" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "First",
         |    "last": "Last"
         |  },
         |  "role": "Director",
         |  "dob": "1998-07-12",
         |  "nino": "AA112233Z"
         |}""".stripMargin)
    val httpRespOK = HttpResponse(OK, Some(validJson))
    val httpRespNOCONTENT = HttpResponse(NO_CONTENT, None)

    "return a JsValue" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespOK)
      connector.getApplicantDetails(testRegId) returns Some(validJson)
    }

    "return None if there is no data for the registration" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespNOCONTENT)
      connector.getApplicantDetails(testRegId) returns None
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getApplicantDetails(testRegId) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getApplicantDetails(testRegId) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServerError)
      connector.getApplicantDetails(testRegId) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", exception)
      connector.getApplicantDetails(testRegId) failedWith exception
    }
  }

  "Calling patchApplicantDetails" should {
    val partialApplicantDetails = ApplicantDetails(
      homeAddress = None,
      emailAddress = None,
      emailVerified = None,
      telephoneNumber = None,
      formerName = None,
      formerNameDate = None,
      previousAddress = None
    )

    val partialJson = Json.obj()

    "return a JsValue with a partial Applicant Details view model" in new Setup {
      mockHttpPATCH[JsValue, JsValue]("tst-url", partialJson)
      connector.patchApplicantDetails(partialApplicantDetails) returns partialJson
    }

    "return a JsValue with a full Applicant Details view model" in new Setup {
      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val fullApplicantDetails: ApplicantDetails = partialApplicantDetails.copy(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        formerName = Some(FormerNameView(false, None)),
        previousAddress = Some(PreviousAddressView(true, None))
      )

      val fullJson = Json.parse(
        s"""
           |{
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  }
           |}""".stripMargin)

      mockHttpPATCH[JsValue, JsValue]("tst-url", fullJson)
      connector.patchApplicantDetails(fullApplicantDetails) returns fullJson
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", notFound)
      connector.patchApplicantDetails(partialApplicantDetails) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", forbidden)
      connector.patchApplicantDetails(partialApplicantDetails) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", internalServerError)
      connector.patchApplicantDetails(partialApplicantDetails) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", exception)
      connector.patchApplicantDetails(partialApplicantDetails) failedWith exception
    }
  }

  "Calling getSicAndCompliance" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "businessDescription": "Test Description",
         |  "labourCompliance": {
         |    "numberOfWorkers": 8,
         |    "temporaryContracts": true,
         |    "skilledWorkers": true
         |  },
         |  "mainBusinessActivity": {
         |    "id": "testId",
         |    "description": "test Desc",
         |    "displayDetails": "test Details"
         |  }
         |}""".stripMargin)
    val httpRespOK = HttpResponse(OK, Some(validJson))
    val httpRespNOCONTENT = HttpResponse(NO_CONTENT, None)

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
      otherBusinessActivities = Some(OtherBusinessActivities(List(SicCode("99889", "otherBusiness", "otherBusiness1")))),
      companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
      workers = Some(Workers(8)),
      temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
      skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
    )

    val json = Json.parse(
      s"""
         |{
         |  "businessDescription": "test Bus Desc",
         |  "labourCompliance": {
         |    "numberOfWorkers": 8,
         |    "temporaryContracts": true,
         |    "skilledWorkers": true
         |  },
         |"otherBusinessActivities": [
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
      mockHttpGET[BankAccount]("tst-url", bankAccount)
      connector.getBankAccount("tstID") returns Some(bankAccount)
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
      mockHttpPATCH[BankAccount, BankAccount]("tst-url", bankAccount)
      connector.patchBankAccount("tstID", bankAccount) returns bankAccount
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", forbidden)
      connector.patchBankAccount("tstID", bankAccount) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", notFound)
      connector.patchBankAccount("tstID", bankAccount) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", internalServiceException)
      connector.patchBankAccount("tstID", bankAccount) failedWith internalServiceException
    }
  }

  "Calling saveTransactionID" should {
    "succeed" when {
      "saving a transactionID" in new Setup {
        mockHttpPATCH[String, HttpResponse]("tst-url", HttpResponse(200))
        val resp: HttpResponse = await(connector.saveTransactionId("tstID", "transID"))

        resp.status mustBe 200
      }
    }
    "fail" when {
      "saving a transactionID" in new Setup {
        mockHttpFailedPATCH[String, HttpResponse]("tst-url", new Upstream4xxResponse("400", 400, 400))
        intercept[Upstream4xxResponse](await(connector.saveTransactionId("tstID", "transID")))
      }
    }
  }

  "getEligibilityData" should {
    "return 200 and a JsObject" in new Setup {
      val json = Json.obj("foo" -> "bar")
      mockHttpGET[HttpResponse]("tst-url", HttpResponse(200, Some(json)))
      connector.getEligibilityData returns json
    }

    "return 404 (no 2xx code) and an execption should be thrown" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getEligibilityData failedWith notFound
    }
  }
}
