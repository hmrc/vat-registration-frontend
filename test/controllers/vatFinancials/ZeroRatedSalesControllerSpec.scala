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

package controllers.vatFinancials

import builders.AuthBuilder
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestZeroRatedSalesController extends ZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.ZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.ZeroRatedSalesController.show()}" should {

    "return HTML when there's a Zero Rated Sales model in S4L" in {
      val zeroRatedSales = ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_YES)

      when(mockS4LService.fetchAndGet[ZeroRatedSales]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(zeroRatedSales)))

      AuthBuilder.submitWithAuthorisedUser(TestZeroRatedSalesController.show(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesRadio" -> ""
      )) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[ZeroRatedSales]()
        (Matchers.eq(S4LKey[ZeroRatedSales]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestZeroRatedSalesController.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[ZeroRatedSales]()
        (Matchers.eq(S4LKey[ZeroRatedSales]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestZeroRatedSalesController.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected Yes" should {

    "return 303" in {
      val returnCacheMapZeroRatedSales = CacheMap("", Map("" -> Json.toJson(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO))))

      when(mockS4LService.saveForm[ZeroRatedSales](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapZeroRatedSales))

      AuthBuilder.submitWithAuthorisedUser(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_YES
      )) {
        _ redirectsTo s"$contextRoot/estimate-zero-rated-sales"
      }

    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO))))
      val returnCacheMapEstimateZeroRatedSales = CacheMap("", Map("" -> Json.toJson(EstimateZeroRatedSales(0L))))

      when(mockS4LService.saveForm[ZeroRatedSales](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      when(mockS4LService.saveForm[EstimateZeroRatedSales](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapEstimateZeroRatedSales))

      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(Future.successful(()))

      AuthBuilder.submitWithAuthorisedUser(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_NO
      ))(_ redirectsTo s"$contextRoot/vat-charge-expectancy")

    }
  }

}
