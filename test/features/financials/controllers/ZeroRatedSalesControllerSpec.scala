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
import models.{CurrentProfile, S4LVatFinancials}
import models.view.vatFinancials.ZeroRatedSales
import models.view.vatFinancials.ZeroRatedSales.{ZERO_RATED_SALES_NO, ZERO_RATED_SALES_YES}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class ZeroRatedSalesControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends ZeroRatedSalesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.ZeroRatedSalesController.show())

  s"GET ${vatFinancials.routes.ZeroRatedSalesController.show()}" should {
    "return HTML when there's a Zero Rated Sales model in S4L" in {
      save4laterReturnsViewModel(ZeroRatedSales(ZERO_RATED_SALES_YES))()

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.show(), fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> "")) {
        _ includesText "Will the company sell any zero-rated goods or services in the next 12 months?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(Controller.show) {
        _ includesText "Will the company sell any zero-rated goods or services in the next 12 months?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[ZeroRatedSales]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(Controller.show) {
        _ includesText "Will the company sell any zero-rated goods or services in the next 12 months?"
      }
    }

  }

  s"POST ${vatFinancials.routes.ZeroRatedSalesController.submit()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with Zero Rated Sales selected Yes" in {
      save4laterExpectsSave[ZeroRatedSales]()

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> ZERO_RATED_SALES_YES)) {
        _ redirectsTo s"$contextRoot/estimate-zero-rated-sales-next-12-months"
      }
    }

    "return 303 with Zero Rated Sales selected No" in {
      save4laterExpectsSave[ZeroRatedSales]()
      save4laterReturns(S4LVatFinancials())
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(dummyCacheMap.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("zeroRatedSalesRadio" -> ZERO_RATED_SALES_NO)) {
        _ redirectsTo s"$contextRoot/expect-to-reclaim-more-vat-than-you-charge"
      }
    }
  }
}
