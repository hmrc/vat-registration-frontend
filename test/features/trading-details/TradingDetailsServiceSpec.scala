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
import features.turnoverEstimates.TurnoverEstimatesService
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models._
import models.external.IncorporationInfo
import models.view.vatTradingDetails.TradingNameView
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.language.postfixOps

class TradingDetailsServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new RegistrationService {
      override val s4LService = mockS4LService
      override val vatRegConnector = mockRegConnector
      override val compRegConnector = mockCompanyRegConnector
      override val incorporationService = mockIncorpInfoService
      override val keystoreConnector = mockKeystoreConnector
      override val turnoverEstimatesService: TurnoverEstimatesService = mockTurnoverEstimatesService
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorporationInfo(any())(any())).thenReturn(OptionT.none[Future, IncorporationInfo])
  }

  "Calling submitTradingDetails" should {
    val s4LTradingDetails = S4LTradingDetails.modelT.toS4LModel(validVatScheme)

    "return a success response when VatTradingDetails is submitted" in new Setup {
      save4laterReturns(s4LTradingDetails)
      when(mockRegConnector.getRegistration(ArgumentMatchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }

    "return a success response when start date choice is BUSINESS_START_DATE" in new Setup {
      val tradingDetailsWithNewTradingName = tradingDetails(tradingName = Some("test"))

      save4laterReturns(s4LTradingDetails.copy(
        tradingName = Some(TradingNameView("yes", Some("test")))
      ))

      when(mockRegConnector.getRegistration(ArgumentMatchers.eq(testRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(tradingDetailsWithNewTradingName.pure)

      service.submitTradingDetails() returns tradingDetailsWithNewTradingName
    }

    "return a success response when VatTradingDetails is submitted and no Trading Name is found in S4L" in new Setup {
      save4laterReturnsNothing[S4LTradingDetails]()
      when(mockRegConnector.getRegistration(ArgumentMatchers.eq(testRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }
  }

}
