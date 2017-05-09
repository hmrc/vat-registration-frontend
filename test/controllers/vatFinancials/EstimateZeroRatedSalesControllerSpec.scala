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
import models.view.vatFinancials.EstimateZeroRatedSales
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class EstimateZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture {



  object TestEstimateZeroRatedSalesController extends EstimateZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.EstimateZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.EstimateZeroRatedSalesController.show()}" should {

    "return HTML Estimate Zero Rated Sales page with no data in the form" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(EstimateZeroRatedSales(100L))))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.show(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedTurnoverEstimate" -> ""
      )) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()
        (Matchers.eq(S4LKey[EstimateZeroRatedSales]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestEstimateZeroRatedSalesController.show) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()
        (Matchers.eq(S4LKey[EstimateZeroRatedSales]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestEstimateZeroRatedSalesController.show) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

  }


  s"POST ${vatFinancials.routes.EstimateZeroRatedSalesController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.EstimateZeroRatedSalesController.submit()} with a valid turnover estimate entered" should {

    "return 303" in {
      val returnCacheMapEstimateZeroRatedSales = CacheMap("", Map("" -> Json.toJson(EstimateZeroRatedSales(100L))))

      when(mockS4LService.saveForm[EstimateZeroRatedSales]
        (any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapEstimateZeroRatedSales))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateZeroRatedSalesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "zeroRatedTurnoverEstimate" -> "60000"
      ))(_ redirectsTo s"$contextRoot/vat-charge-expectancy")

    }
  }

}
