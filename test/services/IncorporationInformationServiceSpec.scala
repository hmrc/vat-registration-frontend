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

package services

import cats.data.OptionT
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.ScrsAddress
import models.external.{CoHoCompanyProfile, CoHoRegisteredOfficeAddress, Name, Officer, OfficerList}
import org.mockito.Mockito._
import org.scalatest.Inspectors

import scala.concurrent.Future

class IncorporationInformationServiceSpec extends VatRegSpec with Inspectors with VatRegistrationFixture {

  private class Setup {
    val service = new IncorporationInformationService {
      override val iiConnector = mockIIConnector
      override val vatRegConnector = mockRegConnector
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

  "getIncorporationInfo" must {
    "return an incorporation info object" in new Setup {
      when(mockRegConnector.getIncorporationInfo(currentProfile().registrationId, currentProfile().transactionId))
        .thenReturn(Future.successful(Some(testIncorporationInfo)))
      service.getIncorporationInfo(currentProfile().registrationId, currentProfile().transactionId) returnsSome testIncorporationInfo
    }
  }
}
