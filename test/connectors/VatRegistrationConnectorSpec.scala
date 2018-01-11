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

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.external.{IncorporationInfo, Name, Officer}
import config.WSHttp
import features.turnoverEstimates.TurnoverEstimates
import features.officer.models.view.LodgingOfficer
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import features.officer.models.view._
import features.sicAndCompliance.models.{BusinessActivityDescription, CompanyProvideWorkers, MainBusinessActivityView, SicAndCompliance, SkilledWorkers, TemporaryContracts, Workers}
import uk.gov.hmrc.http.HttpResponse

import scala.language.postfixOps

class VatRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new RegistrationConnector {
      override val vatRegUrl: String   = "tst-url"
      override val vatRegElUrl: String = "test-url"
      override val http: WSHttp        = mockWSHttp
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
      mockHttpGET[Option[String]]("tst-url", Some("Fake Ref No"))
      connector.getAckRef("tstID") returnsSome "Fake Ref No"
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

  "Calling deleteVatScheme" should {

    val okStatus = new HttpResponse {
      override def status = 200
    }

    val otherRepsonse = new HttpResponse {
      override def status = 500
    }

    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[HttpResponse]("tst-url", okStatus)
      connector.deleteVatScheme("regId")
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", notFound)
      connector.deleteVatScheme("regId") failedWith notFound
    }
  }

  "Calling upsertVatContact" should {

    val vatContact = VatContact(
      digitalContact = VatDigitalContact(email = "test.com", tel = None, mobile = None),
      website = None,
      ppob = scrsAddress)

    "return the correct VatResponse when the microservice completes and returns a VatContact model" in new Setup {
      mockHttpPATCH[VatContact, VatContact]("tst-url", vatContact)
      connector.upsertVatContact("tstID", vatContact) returns vatContact
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatContact, VatContact]("tst-url", forbidden)
      connector.upsertVatContact("tstID", vatContact) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatContact, VatContact]("tst-url", notFound)
      connector.upsertVatContact("tstID", vatContact) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatContact, VatContact]("tst-url", internalServiceException)
      connector.upsertVatContact("tstID", vatContact) failedWith internalServiceException
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

  "Calling upsertVatFrsAnswers" should {

    "return the correct VatResponse when the microservice completes and returns a VatFrsAnswers model" in new Setup {
      mockHttpPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", validVatFlatRateScheme)
      connector.upsertVatFlatRateScheme("tstID", validVatFlatRateScheme) returns validVatFlatRateScheme
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", forbidden)
      connector.upsertVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", notFound)
      connector.upsertVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", internalServiceException)
      connector.upsertVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith internalServiceException
    }
  }


  "Calling getIncorporationInfo" should {

    "return a IncorporationInfo when it can be retrieved from the microservice" in new Setup {
      mockHttpGET[IncorporationInfo]("tst-url", testIncorporationInfo)
      connector.getIncorporationInfo("tstID") returnsSome testIncorporationInfo
    }

    "fail when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[IncorporationInfo]("test-url", notFound)
      connector.getIncorporationInfo("tstID") returnsNone
    }
  }

  "calling submitRegistration" should {
    "return a Success" in new Setup {
      mockHttpPUT[String, HttpResponse]("test-url", validHttpResponse)

      await(connector.submitRegistration("tstID")) mustBe Success
    }
  }

  "getTurnoverEstimates" should {

    val jsonBody = Json.obj("vatTaxable" -> 1000)
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

  "patchTurnoverEstimates" should {

    val turnoverEstimates = TurnoverEstimates(1000L)
    val httpResponse = HttpResponse(200)

    "return a 200 HttpResponse" in new Setup {
      mockHttpPATCH(testUrl, HttpResponse(200))

      val result: HttpResponse = await(connector.patchTurnoverEstimates(turnoverEstimates))
      result.status mustBe httpResponse.status
    }
  }

  "Calling getLodgingOfficer(testRegId)" should {
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
      connector.getLodgingOfficer(testRegId) returns Some(validJson)
    }

    "return None if there is no data for the registration" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespNOCONTENT)
      connector.getLodgingOfficer(testRegId) returns None
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getLodgingOfficer(testRegId) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getLodgingOfficer(testRegId) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServerError)
      connector.getLodgingOfficer(testRegId) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", exception)
      connector.getLodgingOfficer(testRegId) failedWith exception
    }
  }

  "Calling patchLodgingOfficer" should {
    val officer = Officer(
      name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
      role = "Director"
    )

    val partialLodgingOfficer = LodgingOfficer(
      completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
      securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
      homeAddress = None,
      contactDetails = None,
      formerName = None,
      formerNameDate = None,
      previousAddress = None
    )

    val partialJson = Json.parse(
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

    "return a JsValue with a partial Lodging Officer view model" in new Setup {
      mockHttpPATCH[JsValue, JsValue]("tst-url", partialJson)
      connector.patchLodgingOfficer(partialLodgingOfficer) returns partialJson
    }

    "return a JsValue with a full Lodging Officer view model" in new Setup {
      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val fullLodgingOfficer: LodgingOfficer = partialLodgingOfficer.copy(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"))),
        formerName = Some(FormerNameView(false, None)),
        previousAddress = Some(PreviousAddressView(true, None))
      )

      val fullJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z",
           |  "details": {
           |    "currentAddress": {
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test"
           |    }
           |  }
           |}""".stripMargin)

      mockHttpPATCH[JsValue, JsValue]("tst-url", fullJson)
      connector.patchLodgingOfficer(fullLodgingOfficer) returns fullJson
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", notFound)
      connector.patchLodgingOfficer(partialLodgingOfficer) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", forbidden)
      connector.patchLodgingOfficer(partialLodgingOfficer) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", internalServerError)
      connector.patchLodgingOfficer(partialLodgingOfficer) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", exception)
      connector.patchLodgingOfficer(partialLodgingOfficer) failedWith exception
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
}
