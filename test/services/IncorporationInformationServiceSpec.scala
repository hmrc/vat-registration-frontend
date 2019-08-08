/*
 * Copyright 2019 HM Revenue & Customs
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

package services

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{ScrsAddress, SicCode}
import models.external._
import org.mockito.Mockito._
import org.scalatest.Inspectors
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import uk.gov.hmrc.play.test.LogCapturing

import scala.concurrent.Future

class IncorporationInformationServiceSpec extends VatRegSpec with Inspectors with VatRegistrationFixture with LogCapturing {

  private class Setup {
    val service = new IncorporationInformationService {
      override val iiConnector = mockIIConnector
      override val vatRegConnector = mockRegConnector
      override val noneOnsSicCodes: Set[String] = Set("99999", "74990")
    }
  }

  override val officer = Officer(
    name = Name(
      title = Some("Dr"),
      forename = Some("Reddy"),
      otherForenames = Some("Bubbly"),
      surname = "Reddy"
    ),
    role = "director",
    resignedOn = None,
    appointmentLink = None)

  "getOfficerAddressList" must {
    "call IncorporationInformationConnector to get a CoHoRegisteredOfficeAddress" in new Setup {

      val coHoRegisteredOfficeAddress =
        CoHoRegisteredOfficeAddress(
          premises = "premises",
          addressLine1 = "address_line_1",
          addressLine2 = Some("address_line_2"),
          locality = "locality",
          country = Some("country"),
          poBox = Some("po_box"),
          postalCode = Some("postal_code"),
          region = Some("region"))

      val scrsAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))

      when(mockIIConnector.getRegisteredOfficeAddress(currentProfile().transactionId))
        .thenReturn(Future.successful(Some(coHoRegisteredOfficeAddress)))

      service.getRegisteredOfficeAddress returnsSome scrsAddress
    }
  }

  "getOfficerList" must {
    "return a list of officers" in new Setup {
      mockKeystoreFetchAndGet("CompanyProfile", Some(CoHoCompanyProfile("status", "transactionId")))
      when(mockIIConnector.getOfficerList(currentProfile().transactionId))
        .thenReturn(Future.successful(Some(OfficerList(Seq(officer)))))

      service.getOfficerList returns Seq(officer)
    }

    "return am empty sequence when no OfficerList in keystore" in new Setup {
      when(mockIIConnector.getOfficerList(currentProfile().transactionId))
        .thenReturn(Future.successful(Some(OfficerList(Seq()))))
      service.getOfficerList returns Seq.empty[Officer]
    }
  }

  "getIncorpDate" must {
    "return an incorp date" in new Setup {
      when(mockIIConnector.getIncorpUpdate(currentProfile().registrationId, currentProfile().transactionId))
        .thenReturn(Future.successful(Some(
          Json.obj("incorporationDate" -> testIncorporationInfo.statusEvent.incorporationDate.get)))
        )

      service.getIncorpDate(currentProfile().registrationId, currentProfile().transactionId) returns
        testIncorporationInfo.statusEvent.incorporationDate
    }
  }

  "retrieveSicCodes" must {
    "return a sic code if it is available" in new Setup {
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(200, Some(Json.obj("sic_codes" -> Json.arr("testCode")))
          )))

      service.retrieveSicCodes(currentProfile().transactionId) returns
        List("testCode")
    }

    "return an empty list if the json is invalid" in new Setup {
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(200, Some(Json.obj("gARBLED json" -> Json.obj("testCode" -> "incorrect")))
          )))

      service.retrieveSicCodes(currentProfile().transactionId) returns
        List()
    }

    "return multiple valid sic codes if they are available" in new Setup {
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(200, Some(Json.obj("sic_codes" -> Json.arr("testCode", "99999", "testCode2", "testCode3", "74990")))
          )))

      service.retrieveSicCodes(currentProfile().transactionId) returns
        List("testCode", "testCode2", "testCode3")
    }

    "return no sic codes if they are not provided for an unknown reason" in new Setup {
      val json = Json.obj("sic_codes" -> Json.arr())
      val message = s"[incorporation information service][retrieveSicCodes] for txid : ${currentProfile().transactionId} returned List() in JSON"
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(200, Some(json)
          )))


      withCaptureOfLoggingFrom(Logger) { logs =>
        service.retrieveSicCodes(currentProfile().transactionId) returns
          List()
        logs.map(_.getMessage).contains(message) mustBe true
      }
    }

    "return no sic codes with none ons codes provided" in new Setup {
      val json = Json.obj("sic_codes" -> Json.arr("99999", "74990"))
      val message = s"""[incorporation information service][retrieveSicCodes] for txid : ${currentProfile().transactionId} returned List(99999, 74990) in JSON"""
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(200, Some(json)
          )))


      withCaptureOfLoggingFrom(Logger) { logs =>
        service.retrieveSicCodes(currentProfile().transactionId) returns
          List()
        logs.map(_.getMessage).contains(message) mustBe true
      }
    }

    "return no sic codes if they are not available" in new Setup {
      val message = s"[incorporation information service][retrieveSicCodes] for txid : ${currentProfile().transactionId} returned no results"
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.successful(
          HttpResponse(204, None))
        )


      withCaptureOfLoggingFrom(Logger) { logs =>
        service.retrieveSicCodes(currentProfile().transactionId) returns
          List()
        logs.map(_.getMessage).contains(message) mustBe true
      }
    }

    "return no sic codes if the call to II fails" in new Setup {
      val message = s"[incorporation information service][retrieveSicCodes] for txid : ${currentProfile().transactionId} call to II failed with exception II call failed, returning no results"
      when(mockIIConnector.retrieveSicCodes(currentProfile().transactionId))
        .thenReturn(Future.failed(new InternalServerException("II call failed")))

      withCaptureOfLoggingFrom(Logger) { logs =>
        service.retrieveSicCodes(currentProfile().transactionId) returns
          List()
        logs.map(_.getMessage).contains(message) mustBe true
      }
    }
  }
}
