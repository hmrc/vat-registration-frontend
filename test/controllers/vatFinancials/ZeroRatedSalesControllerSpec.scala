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
import play.api.test.FakeRequest

class ZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends ZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.ZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.ZeroRatedSalesController.show()}" should {

    "return HTML when there's a Zero Rated Sales model in S4L" in {
      save4laterReturns2(ZeroRatedSales(ZeroRatedSales.ZERO_RATED_SALES_YES))()

      submitAuthorised(Controller.show(), fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> "")) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Do you expect to make any zero-rated sales?"
      }
    }

  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected Yes" should {

    "return 303" in {
      save4laterExpectsSave[ZeroRatedSales]()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_YES)) {
        _ redirectsTo s"$contextRoot/estimate-zero-rated-sales-next-12-months"
      }
    }
  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()} with Zero Rated Sales selected No" should {

    "return 303" in {
      save4laterExpectsSave[ZeroRatedSales]()
      save4laterExpectsSave[EstimateZeroRatedSales]()
      when(mockVatRegistrationService.deleteElement(any())(any())).thenReturn(().pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> ZeroRatedSales.ZERO_RATED_SALES_NO)) {
        _ redirectsTo s"$contextRoot/expect-to-reclaim-more-vat-than-you-charge"
      }
    }
  }

}
