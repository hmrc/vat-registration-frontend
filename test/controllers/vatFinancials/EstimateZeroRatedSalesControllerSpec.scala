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
import models.view.vatFinancials.EstimateZeroRatedSales
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class EstimateZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {


  object Controller extends EstimateZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.EstimateZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.EstimateZeroRatedSalesController.show()}" should {

    "return HTML Estimate Zero Rated Sales page with no data in the form" in {
      save4laterReturns2(EstimateZeroRatedSales(100L))()

      callAuthorised(Controller.show()) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[EstimateZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[EstimateZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Estimated zero-rated sales for the next 12 months"
      }
    }

  }


  s"POST ${vatFinancials.routes.EstimateZeroRatedSalesController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.EstimateZeroRatedSalesController.submit()} with a valid turnover estimate entered" should {

    "return 303" in {
      save4laterExpectsSave[EstimateZeroRatedSales]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody("zeroRatedTurnoverEstimate" -> "60000")) {
        _ redirectsTo s"$contextRoot/expect-to-reclaim-more-vat-than-you-charge"
      }
    }
  }

}
