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

package controllers.userJourney.sicAndCompliance.cultural

import builders.AuthBuilder
import controllers.userJourney.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.cultural.NotForProfit
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class NotForProfitControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object NotForProfitController extends NotForProfitController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(sicAndCompliance.cultural.routes.NotForProfitController.show())

  s"GET ${sicAndCompliance.cultural.routes.NotForProfitController.show()}" should {

    "return HTML when there's a Not For Profit model in S4L" in {
      val notForProfit = NotForProfit(NotForProfit.NOT_PROFIT_NO)

      when(mockS4LService.fetchAndGet[NotForProfit]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(notForProfit)))

      AuthBuilder.submitWithAuthorisedUser(NotForProfitController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
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
      when(mockS4LService.fetchAndGet[NotForProfit]()
        (Matchers.eq(S4LKey[NotForProfit]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(NotForProfitController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[NotForProfit]()
      (Matchers.eq(S4LKey[NotForProfit]), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(NotForProfitController.show, mockAuthConnector) {
      result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) must include("Is your company a not-for-profit organisation or public body?")
    }
  }

  s"POST ${sicAndCompliance.cultural.routes.NotForProfitController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(NotForProfitController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${sicAndCompliance.cultural.routes.NotForProfitController.submit()} with not for profit Yes selected" should {

    "return 303" in {
      val returnCacheMapNotForProfit = CacheMap("", Map("" -> Json.toJson(NotForProfit(NotForProfit.NOT_PROFIT_YES))))

      when(mockS4LService.saveForm[NotForProfit]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapNotForProfit))

      AuthBuilder.submitWithAuthorisedUser(NotForProfitController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> NotForProfit.NOT_PROFIT_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }

  s"POST ${sicAndCompliance.cultural.routes.NotForProfitController.submit()} with not for profit No selected" should {

    "return 303" in {
      val returnCacheMapNotForProfit = CacheMap("", Map("" -> Json.toJson(NotForProfit(NotForProfit.NOT_PROFIT_NO))))

      when(mockS4LService.saveForm[NotForProfit]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapNotForProfit))

      AuthBuilder.submitWithAuthorisedUser(NotForProfitController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "notForProfitRadio" -> NotForProfit.NOT_PROFIT_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/company-bank-account"
      }

    }
  }
}