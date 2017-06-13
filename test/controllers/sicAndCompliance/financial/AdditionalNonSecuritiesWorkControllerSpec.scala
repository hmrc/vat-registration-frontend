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

package controllers.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class AdditionalNonSecuritiesWorkControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object AdditionalNonSecuritiesWorkController
    extends AdditionalNonSecuritiesWorkController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AdditionalNonSecuritiesWorkController.show())

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  s"GET ${routes.AdditionalNonSecuritiesWorkController.show()}" should {

    "return HTML when there's a Additional Non Securities Work model in S4L" in {
      save4laterReturnsViewModel(AdditionalNonSecuritiesWork(true))()

      submitAuthorised(AdditionalNonSecuritiesWorkController.show(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesRadio" -> "")) {
        _ includesText "Does the company do additional work (excluding securities) " +
          "when introducing a client to a financial service provider?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AdditionalNonSecuritiesWork]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
        _ includesText "Does the company do additional work (excluding securities)" +
          " when introducing a client to a financial service provider?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no dataempty vatScheme" in {
    save4laterReturnsNoViewModel[AdditionalNonSecuritiesWork]()
    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(AdditionalNonSecuritiesWorkController.show) {
        _ includesText "Does the company do additional work (excluding securities) " +
          "when introducing a client to a financial service provider?"
      }
    }
  }

  s"POST ${routes.AdditionalNonSecuritiesWorkController.show()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with Additional Non Securities Work Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterExpectsSave[AdditionalNonSecuritiesWork]()
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()

      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/business-bank-account")
    }

    "return 303 with Additional Non Securities Work No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      save4laterExpectsSave[AdditionalNonSecuritiesWork]()
      save4laterReturnsNoViewModel[BusinessActivityDescription]()

      submitAuthorised(AdditionalNonSecuritiesWorkController.submit(), fakeRequest.withFormUrlEncodedBody(
        "additionalNonSecuritiesWorkRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/provides-discretionary-investment-management-services")

    }
  }
}