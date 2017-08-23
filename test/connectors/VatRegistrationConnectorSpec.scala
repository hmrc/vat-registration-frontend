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
import models.external.IncorporationInfo
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.language.postfixOps

class VatRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new VatRegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "Calling createNewRegistration" should {
    "return a successful outcome when the microservice successfully creates a new Vat Registration" in new Setup {
      mockHttpPOSTEmpty[VatScheme]("tst-url", emptyVatScheme)
      connector.createNewRegistration() returns emptyVatScheme
    }
    "return a Bad Request response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", badRequest)
      connector.createNewRegistration() failedWith badRequest
    }
    "return a Forbidden response" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", forbidden)
      connector.createNewRegistration() failedWith forbidden
    }
    "return an Upstream4xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream4xx)
      connector.createNewRegistration() failedWith upstream4xx
    }
    "return Upstream5xxResponse" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", upstream5xx)
      connector.createNewRegistration() failedWith upstream5xx
    }
    "return a Internal Server Error" in new Setup {
      mockHttpFailedPOSTEmpty[HttpResponse]("tst-url", internalServiceException)
      connector.createNewRegistration() failedWith internalServiceException
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
    "return a successful outcome given an existing registration" in new Setup {
      mockHttpDELETE[Boolean]("tst-url", true)
      connector.deleteVatScheme("regId") completedSuccessfully
    }
    "return the notFound exception when trying to DELETE non-existent registration" in new Setup {
      mockHttpFailedDELETE[Boolean]("tst-url", notFound)
      connector.deleteVatScheme("regId") failedWith notFound
    }
  }

  "Calling upsertVatChoice" should {
    "return the correct VatResponse when the microservice completes and returns a VatChoice model" in new Setup {
      mockHttpPATCH[VatChoice, VatChoice]("tst-url", validVatChoice)
      connector.upsertVatChoice("tstID", validVatChoice) returns validVatChoice
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", forbidden)
      connector.upsertVatChoice("tstID", validVatChoice) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", notFound)
      connector.upsertVatChoice("tstID", validVatChoice) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatChoice, VatChoice]("tst-url", internalServiceException)
      connector.upsertVatChoice("tstID", validVatChoice) failedWith internalServiceException
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

  "Calling upsertVatFinancials" should {
    "return the correct VatResponse when the microservice completes and returns a VatFinancials model" in new Setup {
      mockHttpPATCH[VatFinancials, VatFinancials]("tst-url", validVatFinancials)
      connector.upsertVatFinancials("tstID", validVatFinancials) returns validVatFinancials
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", forbidden)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", notFound)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", internalServiceException)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith internalServiceException
    }
  }

  "Calling upsertSicAndCompliance" should {

    val compliance = VatSicAndCompliance(businessDescription = "", mainBusinessActivity = sicCode)

    "return the correct VatResponse when the microservice completes and returns a SicAndCompliance model" in new Setup {
      mockHttpPATCH[VatFinancials, VatSicAndCompliance]("tst-url", compliance)
      connector.upsertSicAndCompliance("tstID", compliance) returns compliance
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", forbidden)
      connector.upsertSicAndCompliance("tstID", compliance) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", notFound)
      connector.upsertSicAndCompliance("tstID", compliance) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatSicAndCompliance, VatSicAndCompliance]("tst-url", internalServiceException)
      connector.upsertSicAndCompliance("tstID", compliance) failedWith internalServiceException
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

  "Calling upsertVatEligibility" should {

    val vatEligibility = validServiceEligibility

    "return the correct VatResponse when the microservice completes and returns a VatServiceEligibility model" in new Setup {
      mockHttpPATCH[VatServiceEligibility, VatServiceEligibility]("tst-url", vatEligibility)
      connector.upsertVatEligibility("tstID", vatEligibility) returns vatEligibility
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatServiceEligibility, VatServiceEligibility]("tst-url", forbidden)
      connector.upsertVatEligibility("tstID", vatEligibility) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatServiceEligibility, VatServiceEligibility]("tst-url", notFound)
      connector.upsertVatEligibility("tstID", vatEligibility) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatServiceEligibility, VatServiceEligibility]("tst-url", internalServiceException)
      connector.upsertVatEligibility("tstID", vatEligibility) failedWith internalServiceException
    }
  }

  "Calling upsertLodgingOfficer" should {

    val vatLodgingOfficer = validLodgingOfficer

    "return the correct VatResponse when the microservice completes and returns a VatLodgingOfficer model" in new Setup {
      mockHttpPATCH[VatLodgingOfficer, VatLodgingOfficer]("tst-url", vatLodgingOfficer)
      connector.upsertVatLodgingOfficer("tstID", vatLodgingOfficer) returns vatLodgingOfficer
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatLodgingOfficer, VatLodgingOfficer]("tst-url", forbidden)
      connector.upsertVatLodgingOfficer("tstID", vatLodgingOfficer) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatLodgingOfficer, VatLodgingOfficer]("tst-url", notFound)
      connector.upsertVatLodgingOfficer("tstID", vatLodgingOfficer) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatLodgingOfficer, VatLodgingOfficer]("tst-url", internalServiceException)
      connector.upsertVatLodgingOfficer("tstID", vatLodgingOfficer) failedWith internalServiceException
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
}
