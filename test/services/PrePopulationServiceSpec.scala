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

package services

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

import cats.data.OptionT
import connectors.{KeystoreConnector, PPConnector}
import helpers.VatRegSpec
import models.api.{ScrsAddress, VatLodgingOfficer, VatScheme}
import models.external.{AccountingDetails, CoHoCompanyProfile, CoHoRegisteredOfficeAddress, CorporationTaxRegistration}
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.Inspectors
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with Inspectors {

  import cats.instances.future._

  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val none: OptionT[Future, CorporationTaxRegistration] = OptionT.none

    implicit val headerCarrier = HeaderCarrier()
    val mockPPConnector = Mockito.mock(classOf[PPConnector])
    val mockVatRegService = Mockito.mock(classOf[VatRegistrationService])
    val mockIIService = Mockito.mock(classOf[IncorporationInformationService])
    val service = new PrePopulationService(mockPPConnector, mockIIService)(mockVatRegService) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      mockFetchRegId()
    }

  }

  "CT Active Date" must {

    "be a LocalDate" in new Setup {
      val expectedDate = LocalDate.of(2017, 4, 24)
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any())).thenReturn(expectedDate)
      service.getCTActiveDate.value returns Some(expectedDate)
    }

    "be None" in new Setup {
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any())).thenReturn(none)
      service.getCTActiveDate().isEmpty returns true
    }

  }

  "getOfficerAddressList" must {
    "be non-empty if a companyProfile is present" in new Setup {
      val coHoRegisteredOfficeAddress =
        CoHoRegisteredOfficeAddress("premises",
          "address_line_1",
          Some("address_line_2"),
          "locality",
          Some("country"),
          Some("po_box"),
          Some("postal_code"),
          Some("region"))

      val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))
      val emptyVatScheme = VatScheme("123")

      mockKeystoreFetchAndGet[CoHoCompanyProfile]("CompanyProfile", Some(CoHoCompanyProfile("status", "transactionId")))
      when(mockIIService.getRegisteredOfficeAddress("transactionId")).thenReturn(Future.successful(coHoRegisteredOfficeAddress))
      when(mockVatRegService.getVatScheme()).thenReturn(Future.successful(emptyVatScheme))

      service.getOfficerAddressList() returns Seq(scsrAddress)
    }

    "be non-empty if a companyProfile is not present and there is a current address" in new Setup {
      val address = ScrsAddress(line1="street", line2="area", postcode=Some("xyz"))
      val vatSchemeWithAddress = VatScheme("123").copy(lodgingOfficer = Some(VatLodgingOfficer(address)))
      mockKeystoreFetchAndGet[CoHoCompanyProfile]("CompanyProfile", None)
      when(mockVatRegService.getVatScheme()).thenReturn(Future.successful(vatSchemeWithAddress))

      service.getOfficerAddressList() returns Seq(address)
    }

    "be empty if a companyProfile is not present and there is no current address" in new Setup {
      val emptyVatScheme = VatScheme("123")
      mockKeystoreFetchAndGet[CoHoCompanyProfile]("CompanyProfile", None)
      when(mockVatRegService.getVatScheme()).thenReturn(Future.successful(emptyVatScheme))

      service.getOfficerAddressList() returns Seq()
    }
  }

}