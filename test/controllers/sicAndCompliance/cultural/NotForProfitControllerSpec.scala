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

package controllers.sicAndCompliance.cultural

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatSicAndCompliance
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class NotForProfitControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object NotForProfitController extends NotForProfitController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.cultural.routes.NotForProfitController.show())

  s"GET ${sicAndCompliance.cultural.routes.NotForProfitController.show()}" should {

    "return HTML when there's a Not For Profit model in S4L" in {
      save4laterReturnsViewModel(NotForProfit(NotForProfit.NOT_PROFIT_NO))()
      submitAuthorised(NotForProfitController.show(), fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> ""
      )) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[NotForProfit]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(NotForProfitController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[NotForProfit]()

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(NotForProfitController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }
  }

  s"POST ${sicAndCompliance.cultural.routes.NotForProfitController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(NotForProfitController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        result => result isA 400
      }
    }

    "return 303 with not for profit Yes selected" in {
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()

      submitAuthorised(NotForProfitController.submit(), fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> NotForProfit.NOT_PROFIT_YES
      ))(_ redirectsTo s"$contextRoot/business-bank-account")

    }

    "return 303 with not for profit No selected" in {
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturnsViewModel(BusinessActivityDescription("bad"))()

      submitAuthorised(NotForProfitController.submit(), fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> NotForProfit.NOT_PROFIT_NO
      ))(_ redirectsTo s"$contextRoot/business-bank-account")

    }
  }
}