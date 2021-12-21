/*
 * Copyright 2022 HM Revenue & Customs
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

class PrePopulationServiceSpec extends VatRegSpec with Inspectors with S4LMockSugar {

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

  "filterAddressListByCountry" must {
    "return an empty list when 2 addresses in that have a country not in the allowed list of countries" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some(Country(Some("FR"), None)))
      val validAddress2 = validCurrentAddress.copy(country = Some(Country(None, Some("France"))))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq.empty
    }
    "return a list of addresses whereby the countries are united kingdom in several flavours" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some(Country(None, Some("u n i t e d k i n g d o m"))))
      val validAddress2 = validCurrentAddress.copy(country = Some(Country(None, Some(" united KINGDOM "))))
      val validAddress3 = validCurrentAddress.copy(country = Some(Country(Some("uk"), None)))
      val validAddress4 = validCurrentAddress.copy(country = Some(Country(None, Some("United Kingdom"))))
      val seqOfAddresses = validAddress :: validAddress2 :: validAddress3 :: validAddress4 :: Nil

      service.filterAddressListByCountry(seqOfAddresses) mustBe seqOfAddresses
    }
    "return one address whereby 2 exist but one has an invalid country" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some(Country(None, Some("United Kingdom"))))
      val validAddress2 = validCurrentAddress.copy(country = Some(Country(None, Some("invalid country"))))

      service.filterAddressListByCountry(validAddress :: validAddress2 :: Nil) mustBe Seq(validAddress)
    }
    "return 2 addresses whereby 1 of the addresses has country of None" in new Setup {
      val validAddress = validCurrentAddress.copy(country = Some(Country(None, Some("United Kingdom"))))
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
