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
import connectors.KeystoreConnector
import helpers.{S4LMockSugar, VatRegSpec}
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration, Officer}
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerHomeAddressView}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.Inspectors

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with Inspectors with S4LMockSugar {

  val officerName = Name(Some("Reddy"), None, "Yattapu" , Some("Dr"))


  import cats.instances.future._
  import cats.syntax.applicative._

  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val service = new PrePopulationService(mockPPConnector, mockIIService, mockS4LService) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      mockFetchRegId()
    }

  }

  "CT Active Date" must {

    "be a LocalDate" in new Setup {
      val expectedDate = LocalDate.of(2017, 4, 24)
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any())).thenReturn(expectedDate)
      service.getCTActiveDate returnsSome expectedDate
    }

    "be None" in new Setup {
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any()))
        .thenReturn(OptionT.none[Future, CorporationTaxRegistration])
      service.getCTActiveDate().returnsNone
    }

  }

  "getOfficerAddressList" must {

    "be non-empty when companyProfile, addressDB and addresS4L are present" in new Setup {
      val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))
      val emptyVatScheme = VatScheme("123")
      val officerHomeAddressView = OfficerHomeAddressView(scsrAddress.id, Some(scsrAddress))

      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(scsrAddress))
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns[OfficerHomeAddressView](officerHomeAddressView)

      service.getOfficerAddressList() returns Seq(scsrAddress)
    }

    "be non-empty if a companyProfile is not present but addressDB exists" in new Setup {
      val address = ScrsAddress(line1 = "street", line2 = "area", postcode = Some("xyz"))
      val vatSchemeWithAddress = VatScheme("123").copy(lodgingOfficer = Some(VatLodgingOfficer(address, DateOfBirth.empty, "", "director", officerName)))

      when(mockVatRegistrationService.getVatScheme()).thenReturn(vatSchemeWithAddress.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(address))
      save4laterReturnsNothing[OfficerHomeAddressView]()

      service.getOfficerAddressList() returns Seq(address)
    }

    "be empty if a companyProfile is not present and addressDB and addresS4L are not present" in new Setup {
      val emptyVatScheme = VatScheme("123")

      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.none[Future, ScrsAddress])
      save4laterReturnsNothing[OfficerHomeAddressView]()

      service.getOfficerAddressList() returns Seq()
    }
  }

  "getOfficerList" must {

    "be non-empty when OfficerList are present" in new Setup {
      val officer = Officer(officerName, "director", None, None)
      val emptyVatScheme = VatScheme("123")
      val completeCapacityView = CompletionCapacityView(officerName.id, Some(officer))

      val seqOfficers : Seq[Officer] = Seq(officer)

      when(mockIIService.getOfficerList()).thenReturn(OptionT.pure(seqOfficers))
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns[CompletionCapacityView](completeCapacityView)

      service.getOfficerList() returns seqOfficers
    }

  }


}