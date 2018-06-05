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
import utils.SystemDate

import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with VatRegistrationFixture with Inspectors with S4LMockSugar {
  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val service = new PrePopService {
      override val businessRegistrationConnector = mockBrConnector
      override val incorpInfoService = mockIIService
      override val vatRegService = mockVatRegistrationService
      override val save4later = mockS4LService
      mockFetchRegId()
    }
  }

  val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def optCtr = CorporationTaxRegistration(
    Some(AccountingDetails("", Some(SystemDate.getSystemDate.toLocalDate.plusDays(7) format expectedFormat))))

  "CT Active Date" must {

    "be a LocalDate" in new Setup {
      val expectedDate = Some(SystemDate.getSystemDate.toLocalDate.plusDays(7))
      when(mockVatRegistrationService.getThreshold(any())(any()))
        .thenReturn(Future.successful(Threshold(false, Some(Threshold.INTENDS_TO_SELL))))

      service.getCTActiveDate returns expectedDate
    }

    "be None" in new Setup {
      when(mockVatRegistrationService.getThreshold(any())(any()))
        .thenReturn(Future.successful(Threshold(false, Some(Threshold.SELLS))))
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

    "be non-empty when companyProfile, addressDB and addressS4L are present" in new Setup {
      val ppobView = PpobView(scrsAddress.id, Some(scrsAddress))

      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(Some(scrsAddress)))
      when(mockVatRegistrationService.getVatScheme)
        .thenReturn(emptyVatScheme.pure)

      save4laterReturns(BusinessContact(ppobAddress = Some(scrsAddress)))

      service.getPpobAddressList returns Seq(scrsAddress)
    }
    "return 1 address when II returns an address that does not have the country of UK, but s4l returns 1 with a country of UK" in new Setup {
      val validAddrWithUKCountry = ScrsAddress("myLine1","myLine2", None,Some("myLine4"),Some("XX XY"),Some("UK"))
      when(mockIIService.getRegisteredOfficeAddress)
        .thenReturn(Future.successful(Some(scrsAddress.copy(country = Some("foo BAR")))))
      when(mockVatRegistrationService.getVatScheme)
        .thenReturn(emptyVatScheme.pure)

      save4laterReturns(BusinessContact(ppobAddress = Some(validAddrWithUKCountry)))

      service.getPpobAddressList returns Seq(validAddrWithUKCountry)
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

  "filterAddressListByCountry" must {
    "return an empty list when 2 addresses in that have a country not in the allowed list of countries" in new Setup {
      val validAddress   = validCurrentAddress.copy(country = Some("foo bar wizz bang"))
      val validAddress2  = validCurrentAddress.copy(country = Some("wizz wollop kablam"))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq.empty
    }
    "return a list of addresses whereby the countries are united kingdom in several flavours" in new Setup {
      val validAddress   =  validCurrentAddress.copy(country = Some("u n i t e d k i n g d o m"))
      val validAddress2  =  validCurrentAddress.copy(country = Some(" united KINGDOM "))
      val validAddress3  =  validCurrentAddress.copy(country = Some("uk"))
      val validAddress4  =  validCurrentAddress.copy(country = Some("United Kingdom"))
      val seqOfAddresses =  validAddress :: validAddress2 :: validAddress3 :: validAddress4 :: Nil

      service.filterAddressListByCountry(seqOfAddresses) mustBe seqOfAddresses
    }
    "return one address whereby 2 exist but one has an invalid country" in new Setup {
      val validAddress   =  validCurrentAddress.copy(country = Some("United Kingdom"))
      val validAddress2  =  validCurrentAddress.copy(country = Some("invalid country"))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq(validAddress)
    }
    "return 2 addresses whereby 1 of the addresses has country of None" in new Setup {
      val validAddress   =  validCurrentAddress.copy(country = Some("United Kingdom"))
      val validAddress2  =  validCurrentAddress.copy(country = None)
      val seqOfAddresses =  validAddress :: validAddress2 :: Nil

      service.filterAddressListByCountry(seqOfAddresses) mustBe seqOfAddresses
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

  "getCompanyRegistration" should {
    "return a valid company registration" when {
      "company intends to sell" in new Setup {
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.successful(generateThreshold(reason = Some(Threshold.INTENDS_TO_SELL))))

        service.getCompanyRegistrationDetails returnsSome optCtr
      }
    }
    "return None" when {
      "there is no threshold reason" in new Setup {
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.successful(validVoluntaryRegistration))

        service.getCompanyRegistrationDetails returnsNone
      }
      "company already sells" in new Setup {
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.successful(generateThreshold(reason = Some(Threshold.SELLS))))

        service.getCompanyRegistrationDetails returnsNone
      }
    }
    "return an Error" when {
      val failure  = new IllegalArgumentException("Threshold Fails")
      "an error happens getting the threshold" in new Setup{
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.failed(failure))

        service.getCompanyRegistrationDetails failedWith failure
      }
    }
  }

  "getTradingName" should {
    "return a trading name if found" in new Setup {
      when(mockBrConnector.retrieveTradingName(any())(any()))
        .thenReturn(Future.successful(Some("Foo Bar")))

      service.getTradingName("someRegId") returnsSome "Foo Bar"
    }
    "return None if not found" in new Setup {
      when(mockBrConnector.retrieveTradingName(any())(any()))
        .thenReturn(Future.successful(None))

      service.getTradingName("someRegId") returnsNone
    }
  }

  "saveTradingName" should {
    "return the trading name that was passed in" in new Setup {
      when(mockBrConnector.upsertTradingName(any(), any())(any()))
        .thenReturn(Future.successful("Foo Bar Wizz"))

      service.saveTradingName("someRegId", "Foo Bar Wizz") returns "Foo Bar Wizz"
    }
  }
}
