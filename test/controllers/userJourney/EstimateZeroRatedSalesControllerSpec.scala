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

package controllers.userJourney

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CacheKey
import models.view.EstimateZeroRatedSales
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

class EstimateZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestEstimateZeroRatedSalesController extends EstimateZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.EstimateZeroRatedSalesController.show())

  s"GET ${routes.EstimateZeroRatedSalesController.show()}" should {

    "return HTML Estimate Zero Rated Sales page with no data in the form" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(EstimateZeroRatedSales(100L))))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesEstimate" -> ""
      )) {

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Estimated zero-rated sales for the next 12 months")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()
        (Matchers.eq(CacheKey[EstimateZeroRatedSales]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestEstimateZeroRatedSalesController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Estimated zero-rated sales for the next 12 months")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()
        (Matchers.eq(CacheKey[EstimateZeroRatedSales]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestEstimateZeroRatedSalesController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Estimated zero-rated sales for the next 12 months")
      }
    }

  }


  s"POST ${routes.EstimateZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }
    }
  }

  s"POST ${routes.EstimateZeroRatedSalesController.submit()} with a valid turnover estimate entered" should {

    "return 303" in {
      val returnCacheMapEstimateZeroRatedSales = CacheMap("", Map("" -> Json.toJson(EstimateZeroRatedSales(100L))))

      when(mockS4LService.saveForm[EstimateZeroRatedSales]
        (Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMapEstimateZeroRatedSales))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesEstimate" -> "60000"
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe s"${contextRoot}/vat-charge-expectancy"
      }

    }
  }

}
