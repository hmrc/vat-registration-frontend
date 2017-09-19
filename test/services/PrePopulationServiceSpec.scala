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
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration, Officer}
import models.view.vatContact.ppob.PpobView
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerHomeAddressView, OfficerSecurityQuestionsView}
import models.{S4LVatContact, S4LVatLodgingOfficer}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.Inspectors

import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with VatRegistrationFixture with Inspectors with S4LMockSugar {

  override val officerName = Name(Some("Bob" ), Some("Bimbly Bobblous"), "Bobbings", None)

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
      when(mockPPConnector.getCompanyRegistrationDetails(any(), any(), any())).thenReturn(expectedDate)
      service.getCTActiveDate returnsSome expectedDate
    }

    "be None" in new Setup {
      when(mockPPConnector.getCompanyRegistrationDetails(any(), any(), any()))
        .thenReturn(OptionT.none[Future, CorporationTaxRegistration])
      service.getCTActiveDate().returnsNone
    }

  }

  "getOfficerAddressList" must {
    val emptyVatScheme = VatScheme("123")

    "be non-empty when companyProfile, addressDB and addressS4L are present" in new Setup {
      val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))
      val officerHomeAddressView = OfficerHomeAddressView(scsrAddress.id, Some(scsrAddress))

      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(scsrAddress))
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns(S4LVatLodgingOfficer(officerHomeAddress = Some(officerHomeAddressView)))

      service.getOfficerAddressList() returns Seq(scsrAddress)
    }

    "be non-empty if a companyProfile is not present but addressDB exists" in new Setup {
      val address = ScrsAddress(line1 = "street", line2 = "area", postcode = Some("xyz"))
      val vatSchemeWithAddress = VatScheme("123").copy(lodgingOfficer = Some(VatLodgingOfficer(
        address, validDob, "", "director", officerName, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)))

      when(mockVatRegistrationService.getVatScheme()).thenReturn(vatSchemeWithAddress.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(address))
      save4laterReturnsNothing[S4LVatLodgingOfficer]()

      service.getOfficerAddressList() returns Seq(address)
    }

    "be empty if a companyProfile is not present and addressDB and addressS4L are not present" in new Setup {

      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.none[Future, ScrsAddress])
      save4laterReturnsNothing[S4LVatLodgingOfficer]()

      service.getOfficerAddressList() returns Seq()
    }
  }

  "getPpobAddressList" must {
    val emptyVatScheme = VatScheme("123")
    val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))

    "be non-empty when companyProfile, addressDB and addressS4L are present" in new Setup {
      val ppobView = PpobView(scsrAddress.id, Some(scsrAddress))

      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(scsrAddress))
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns(S4LVatContact(ppob = Some(ppobView)))

      service.getPpobAddressList() returns Seq(scsrAddress)
    }

    "be non-empty if a companyProfile is not present but addressDB exists" in new Setup {
      val address = ScrsAddress(line1 = "street", line2 = "area", postcode = Some("xyz"))
      val vatSchemeWithAddress = VatScheme("123").copy(vatContact = Some(validVatContact.copy(ppob = scsrAddress)))

      when(mockVatRegistrationService.getVatScheme()).thenReturn(vatSchemeWithAddress.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.pure(address))
      save4laterReturnsNothing[S4LVatContact]()

      service.getPpobAddressList() returns Seq(address, scsrAddress)
    }

    "be empty if a companyProfile is not present and addressDB and addressS4L are not present" in new Setup {

      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      when(mockIIService.getRegisteredOfficeAddress()).thenReturn(OptionT.none[Future, ScrsAddress])
      save4laterReturnsNothing[S4LVatContact]()

      service.getPpobAddressList() returns Seq()
    }
  }

  "getOfficerList" must {
    val officer = Officer(officerName, "director", Some(validDob), None, None)
    // S4L
    val completeCapacityView = CompletionCapacityView(completionCapacity.name.id, Some(completionCapacity))
    // BE
    val emptyVatScheme = VatScheme("123")
    val address = ScrsAddress(line1 = "street", line2 = "area", postcode = Some("xyz"))

    "be non-empty when OfficerList is present and nothing in S4L" in new Setup {

      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      when(mockIIService.getOfficerList()).thenReturn(Seq(officer).pure)
      save4laterReturnsNothing[S4LVatLodgingOfficer]

      service.getOfficerList() returns Seq(officer)
    }

    "be non-empty when officer only in S4L" in new Setup {

      when(mockIIService.getOfficerList()).thenReturn(Seq.empty[Officer].pure)
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns(S4LVatLodgingOfficer(completionCapacity = Some(completeCapacityView),
        officerSecurityQuestions = Some(OfficerSecurityQuestionsView(LocalDate.of(1999,1,1), ""))))

      service.getOfficerList() returns Seq(officer)
    }

    "be non-empty when officer only in BE" in new Setup {
      val testRole = "director"
      val testDob = DateOfBirth(1, 2, 1984)
      val testName = officerName
      val vatSchemeWithOfficer = VatScheme("123").copy(lodgingOfficer = Some(VatLodgingOfficer(
        address, testDob, "nino", testRole, testName, changeOfName, currentOrPreviousAddress, validOfficerContactDetails)))

      when(mockIIService.getOfficerList()).thenReturn(Seq.empty[Officer].pure)
      when(mockVatRegistrationService.getVatScheme()).thenReturn(vatSchemeWithOfficer.pure)
      save4laterReturnsNothing[S4LVatLodgingOfficer]

      service.getOfficerList() returns Seq(Officer(testName, testRole, Some(testDob)))
    }

    "be non-empty and no duplicates when OfficerList and same officer in S4L are present" in new Setup {

      when(mockIIService.getOfficerList()).thenReturn(Seq(officer).pure)
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturns(S4LVatLodgingOfficer(completionCapacity = Some(completeCapacityView)))

      service.getOfficerList() returns Seq(officer)
    }

    "be empty when no officer list is present" in new Setup {

      when(mockIIService.getOfficerList()).thenReturn(Seq.empty[Officer].pure)
      when(mockVatRegistrationService.getVatScheme()).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatLodgingOfficer]

      service.getOfficerList() returns Seq()
    }

 }
}