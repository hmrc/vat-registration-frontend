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

package controllers.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatSicAndCompliance
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AdditionalNonSecuritiesWorkControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object AdditionalNonSecuritiesWorkController extends AdditionalNonSecuritiesWorkController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(routes.AdditionalNonSecuritiesWorkController.show())

  s"GET ${routes.AdditionalNonSecuritiesWorkController.show()}" should {
    "return HTML when there's a Additional Non Securities Work model in S4L" in {
      save4laterReturnsViewModel(AdditionalNonSecuritiesWork(true))()

      mockGetCurrentProfile()
      submitAuthorised(AdditionalNonSecuritiesWorkController.show(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesRadio" -> "")) {
        _ includesText "Does the company do additional work (excluding securities) " +
          "when introducing a client to a financial service provider?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AdditionalNonSecuritiesWork]()
      when(mockVatRegistrationService.getVatScheme(any(), any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()

      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
        _ includesText "Does the company do additional work (excluding securities)" +
          " when introducing a client to a financial service provider?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no dataempty vatScheme" in {
    save4laterReturnsNoViewModel[AdditionalNonSecuritiesWork]()
    when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    mockGetCurrentProfile()
      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
        _ includesText "Does the company do additional work (excluding securities) " +
          "when introducing a client to a financial service provider?"
      }
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.show()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()
      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with Additional Non Securities Work Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      save4laterExpectsSave[AdditionalNonSecuritiesWork]()
      mockGetCurrentProfile()
      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }

    "return 303 with Additional Non Securities Work No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme(any(), any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterExpectsSave[AdditionalNonSecuritiesWork]()
      mockGetCurrentProfile()
      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/provides-discretionary-investment-management-services")

    }
  }
}
