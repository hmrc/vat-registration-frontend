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

package services

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

import cats.data.OptionT
import fixtures.VatRegistrationFixture
import models.BusinessContact
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.Inspectors
import testHelpers.{S4LMockSugar, VatRegSpec}
import utils.SystemDate

import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with VatRegistrationFixture with Inspectors with S4LMockSugar {

  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val service: PrePopulationService = new PrePopulationService(
      mockS4LService,
      mockBusinessContactService,
      mockVatRegistrationService
    ) {
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
        .thenReturn(Future.successful(Threshold(false)))

      service.getCTActiveDate returns expectedDate
    }

    "be None" in new Setup {
      when(mockVatRegistrationService.getThreshold(any())(any()))
        .thenReturn(Future.successful(Threshold(true, Some(SystemDate.getSystemDate.toLocalDate.minusDays(20)))))
      service.getCTActiveDate returns None
    }

  }

  "getPpobAddressList" must {
    "be non-empty when companyProfile, addressDB and addressS4L are present" in new Setup {
      when(mockBusinessContactService.getBusinessContact(any(), any(), any()))
        .thenReturn(BusinessContact().pure)

      save4laterReturns(BusinessContact(ppobAddress = Some(scrsAddress)))

      service.getPpobAddressList returns Seq(scrsAddress)
    }
    "return 1 address when II returns an address that does not have the country of UK, but s4l returns 1 with a country of UK" in new Setup {
      val validAddrWithUKCountry = ScrsAddress("myLine1", "myLine2", None, Some("myLine4"), Some("XX XY"), Some("UK"))

      when(mockBusinessContactService.getBusinessContact(any(), any(), any()))
        .thenReturn(BusinessContact().pure)

      save4laterReturns(BusinessContact(ppobAddress = Some(validAddrWithUKCountry)))

      service.getPpobAddressList returns Seq(validAddrWithUKCountry)
    }

    "be non-empty if a companyProfile is not present but addressDB exists" in new Setup {
      when(mockBusinessContactService.getBusinessContact(any(), any(), any()))
        .thenReturn(BusinessContact(ppobAddress = Some(scrsAddress)).pure)

      save4laterReturnsNothing[BusinessContact]()

      service.getPpobAddressList returns Seq(scrsAddress)
    }

    "be empty if a companyProfile is not present and addressDB and addressS4L are not present" in new Setup {

      when(mockBusinessContactService.getBusinessContact(any(), any(), any()))
        .thenReturn(BusinessContact().pure)

      save4laterReturnsNothing[BusinessContact]()

      service.getPpobAddressList returns Seq()
    }
  }

  "filterAddressListByCountry" must {
    "return an empty list when 2 addresses in that have a country not in the allowed list of countries" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some("foo bar wizz bang"))
      val validAddress2 = validCurrentAddress.copy(country = Some("wizz wollop kablam"))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq.empty
    }
    "return a list of addresses whereby the countries are united kingdom in several flavours" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some("u n i t e d k i n g d o m"))
      val validAddress2 = validCurrentAddress.copy(country = Some(" united KINGDOM "))
      val validAddress3 = validCurrentAddress.copy(country = Some("uk"))
      val validAddress4 = validCurrentAddress.copy(country = Some("United Kingdom"))
      val seqOfAddresses = validAddress :: validAddress2 :: validAddress3 :: validAddress4 :: Nil

      service.filterAddressListByCountry(seqOfAddresses) mustBe seqOfAddresses
    }
    "return one address whereby 2 exist but one has an invalid country" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some("United Kingdom"))
      val validAddress2 = validCurrentAddress.copy(country = Some("invalid country"))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq(validAddress)
    }
    "return 2 addresses whereby 1 of the addresses has country of None" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some("United Kingdom"))
      val validAddress2 = validCurrentAddress.copy(country = None)
      val seqOfAddresses = validAddress :: validAddress2 :: Nil

      service.filterAddressListByCountry(seqOfAddresses) mustBe seqOfAddresses
    }
  }

  "getCompanyRegistration" should {
    "return a valid company registration" when {
      "it is a voluntary registration" in new Setup {
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.successful(generateThreshold(false)))

        service.getCompanyRegistrationDetails returnsSome optCtr
      }
    }
    "return an Error" when {
      val failure = new IllegalArgumentException("Threshold Fails")
      "an error happens getting the threshold" in new Setup {
        when(mockVatRegistrationService.getThreshold(any())(any()))
          .thenReturn(Future.failed(failure))

        service.getCompanyRegistrationDetails failedWith failure
      }
    }
  }
}
