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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

import cats.data.OptionT
import common.enums.VatRegStatus
import features.businessContact.models.BusinessContact
import features.officer.models.view._
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration, Officer}
import models.view.vatContact.ppob.PpobView
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.Inspectors

import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with VatRegistrationFixture with Inspectors with S4LMockSugar {
  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val service = new PrePopService {
      override val ppConnector = mockPPConnector
      override val incorpInfoService = mockIIService
      override val vatRegService = mockVatRegistrationService
      override val save4later = mockS4LService
      mockFetchRegId()
    }
  }

  val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val optCtr = CorporationTaxRegistration(
    Some(AccountingDetails("", Some(LocalDate.of(2017, 4, 24) format expectedFormat))))

  "CT Active Date" must {

    "be a LocalDate" in new Setup {
      val expectedDate = Some(LocalDate.of(2017, 4, 24))
      when(mockPPConnector.getCompanyRegistrationDetails(any(), any(), any()))
        .thenReturn(Future.successful(Some(optCtr)))
      service.getCTActiveDate returns expectedDate
    }

    "be None" in new Setup {
      when(mockPPConnector.getCompanyRegistrationDetails(any(), any(), any()))
        .thenReturn(Future.successful(None))
      service.getCTActiveDate returns None
    }

  }

  "getOfficerAddressList" must {
    val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
    val lodgingOfficer = LodgingOfficer(
      completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
      securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
      formerName = Some(FormerNameView(false, None)),
      formerNameDate = None,
      previousAddress = Some(PreviousAddressView(false, None))
    )

    "be non-empty when companyProfile, address are present" in new Setup {
      val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))
      val officerHomeAddressView = HomeAddressView(scsrAddress.id, Some(scsrAddress))

      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(Some(scsrAddress)))

      service.getOfficerAddressList(lodgingOfficer) returns Seq(scsrAddress, currentAddress)
    }

    "be non-empty if a companyProfile is not present but address exists" in new Setup {
      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(None))

      service.getOfficerAddressList(lodgingOfficer) returns Seq(currentAddress)
    }

    "be empty if a companyProfile is not present and address are not present" in new Setup {
      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(None))

      val emptyLodgingOfficer = LodgingOfficer(None, None, None, None, None, None, None)

      service.getOfficerAddressList(emptyLodgingOfficer) returns Seq()
    }
  }

  "getPpobAddressList" must {
    val emptyVatScheme = VatScheme("123", status = VatRegStatus.draft)
    val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"), Some("region"), Some("postal_code"), Some("country"))

    "be non-empty when companyProfile, addressDB and addressS4L are present" in new Setup {
      val ppobView = PpobView(scsrAddress.id, Some(scsrAddress))

      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(Some(scrsAddress)))

      when(mockVatRegistrationService.getVatScheme)
        .thenReturn(emptyVatScheme.pure)

      save4laterReturns(BusinessContact(ppobAddress = Some(scrsAddress)))

      service.getPpobAddressList returns Seq(scrsAddress)
    }

    "be non-empty if a companyProfile is not present but addressDB exists" in new Setup {
      val address = ScrsAddress(line1 = "street", line2 = "area", postcode = Some("xyz"))
      val vatSchemeWithAddress = VatScheme(
        "123",
        status = VatRegStatus.draft
      ).copy(
        businessContact = Some(BusinessContact(ppobAddress = Some(scrsAddress)))
      )

      when(mockVatRegistrationService.getVatScheme)
        .thenReturn(vatSchemeWithAddress.pure)

      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(Some(scrsAddress)))

      save4laterReturnsNothing[BusinessContact]()

      service.getPpobAddressList returns Seq(scrsAddress)
    }

    "be empty if a companyProfile is not present and addressDB and addressS4L are not present" in new Setup {

      when(mockVatRegistrationService.getVatScheme)
        .thenReturn(emptyVatScheme.pure)

      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(None))

      save4laterReturnsNothing[BusinessContact]()

      service.getPpobAddressList returns Seq()
    }
  }

  "getOfficerList" must {
    "be empty when no officer list is present" in new Setup {
      when(mockIIService.getOfficerList).thenReturn(Seq.empty[Officer].pure)

      service.getOfficerList returns Seq()
    }

    "return the II officer list is present" in new Setup {
      when(mockIIService.getOfficerList).thenReturn(Seq.empty[Officer].pure)

      service.getOfficerList returns Seq()
    }
 }
}
