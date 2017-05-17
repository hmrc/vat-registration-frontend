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

import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class ZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  import cats.instances.future._
  import cats.syntax.applicative._

  object TestZeroRatedSalesController extends ZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.ZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.ZeroRatedSalesController.show()}" should {

    "return HTML when there's a Zero Rated Sales model in S4L" in {
      save4laterReturns(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_YES))

      submitAuthorised(TestZeroRatedSalesController.show(), fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> "")) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestZeroRatedSalesController.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(TestZeroRatedSalesController.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected Yes" should {

    "return 303" in {
      val returnCacheMapZeroRatedSales = CacheMap("", Map("" -> Json.toJson(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO))))

      when(mockS4LService.saveForm[ZeroRatedSales](any())(any(), any(), any()))
        .thenReturn(returnCacheMapZeroRatedSales.pure)

      submitAuthorised(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_YES
      )) {
        _ redirectsTo s"$contextRoot/estimate-zero-rated-sales"
      }
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected No" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_NO))))
      val zeroCacheMap = CacheMap("", Map("" -> Json.toJson(EstimateZeroRatedSales(0L))))

      when(mockS4LService.saveForm[ZeroRatedSales](any())(any(), any(), any())).thenReturn(returnCacheMap.pure)
      when(mockS4LService.saveForm[EstimateZeroRatedSales](any())(any(), any(), any())).thenReturn(zeroCacheMap.pure)
      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)

      submitAuthorised(TestZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_NO
      ))(_ redirectsTo s"$contextRoot/vat-charge-expectancy")
    }
  }

}
