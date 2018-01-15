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

package connectors

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.Threshold
import models.external.{AccountingDetails, CorporationTaxRegistration}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import services.{RegistrationService, S4LService}

import scala.concurrent.Future

class PPConnectorSpec extends VatRegSpec with VatRegistrationFixture with MockitoSugar{

  class Setup {
    val connector = new PPConnector {
      override val s4l: S4LService = mockS4LService
      override val vrs: RegistrationService = mockVatRegistrationService
    }
  }

  val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val expectedCompanyReg = CorporationTaxRegistration(
    Some(AccountingDetails("", Some(LocalDate.now.plusDays(7) format expectedFormat))))


  "getCompanyRegistration" should {
    "return a valid company registration" when {
      "company intends to sell" in new Setup {
        when(mockVatRegistrationService.getThreshold(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(generateThreshold(reason = Some(Threshold.INTENDS_TO_SELL))))

        connector.getCompanyRegistrationDetails returnsSome expectedCompanyReg
      }
    }
    "return None" when {
      "there is no threshold reason" in new Setup {
        when(mockVatRegistrationService.getThreshold(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(validVoluntaryRegistration))

        connector.getCompanyRegistrationDetails returnsNone
      }
      "company already sells" in new Setup {
        when(mockVatRegistrationService.getThreshold(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(generateThreshold(reason = Some(Threshold.SELLS))))

        connector.getCompanyRegistrationDetails returnsNone
      }
    }
    "return an Error" when {
      val failure  = new IllegalArgumentException("Threshold Fails")
      "an error happens getting the threshold" in new Setup{
        when(mockVatRegistrationService.getThreshold(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.failed(failure))

        connector.getCompanyRegistrationDetails failedWith failure
      }
    }
  }


}
