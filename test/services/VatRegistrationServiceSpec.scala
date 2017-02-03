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

import connectors.VatRegistrationConnector
import enums.DownstreamOutcome
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatTradingDetails}
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture {

    implicit val hc = HeaderCarrier()
    val mockRegConnector = mock[VatRegistrationConnector]

    class Setup {
      val service = new VatRegistrationService(mockRegConnector)
    }

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created" in new Setup {
      when(mockRegConnector.createNewRegistration()(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))
      service.assertRegistrationFootprint().map(_ mustBe DownstreamOutcome.Success)
    }
  }

  "Calling submitVatChoice" should {
    "return a success response when a VatChoice is submitted" in new Setup {
      when(mockRegConnector.upsertVatChoice(validRegId, validVatChoice))
        .thenReturn(Future.successful(validVatChoice))
      service.submitVatChoice(validStartDate).map(_ mustBe VatChoice)
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      when(mockRegConnector.upsertVatTradingDetails(validRegId, validVatTradingDetails))
        .thenReturn(Future.successful(validVatTradingDetails))
      service.submitTradingDetails(validTradingName).map(_ mustBe VatTradingDetails)
    }
  }

  "Calling registrationToSummary" should {
    "convert a VAT Registration API Model to a summary model" in new Setup {
      service.registrationToSummary(validVatScheme) mustBe validSummaryView
    }

  }

  "Calling getRegistrationSummary" should {
    "return a defined Summary when the connector returns a valid VAT Registration response" ignore new Setup {
      when(mockRegConnector.getRegistration(validRegId))
        .thenReturn(Future.successful(validVatScheme))
      service.getRegistrationSummary() mustBe Future.successful(validSummaryView)
    }
  }

}
