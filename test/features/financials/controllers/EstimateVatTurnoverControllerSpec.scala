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

import connectors.KeystoreConnector
import controllers.vatFinancials
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.view.vatFinancials.EstimateVatTurnover
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class EstimateVatTurnoverControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends EstimateVatTurnoverController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.EstimateVatTurnoverController.show())

  s"GET ${vatFinancials.routes.EstimateVatTurnoverController.show()}" should {
    "return HTML Estimate Vat Turnover page" in {
      save4laterReturnsViewModel(EstimateVatTurnover(100L))()

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(Controller.show()) {
        _ includesText "What will the company&#x27;s VAT taxable turnover be during the next 12 months?"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(validVatScheme.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(Controller.show) {
        _ includesText "What will the company&#x27;s VAT taxable turnover be during the next 12 months?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(Controller.show) {
        _ includesText "What will the company&#x27;s VAT taxable turnover be during the next 12 months?"
      }
    }
  }

  s"POST ${vatFinancials.routes.EstimateVatTurnoverController.submit()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with a valid turnover estimate entered" in {
      save4laterReturnsViewModel(EstimateVatTurnover(0L))()
      save4laterExpectsSave[EstimateVatTurnover]()
      mockKeystoreCache[Long](EstimateVatTurnoverKey.lastKnownValueKey, dummyCacheMap)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody("turnoverEstimate" -> "50000")) {
        _ redirectsTo s"$contextRoot/sell-zero-rated-items-next-12-months"
      }
    }

    "return 303 with no valid turnover estimate entered" in {
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      save4laterExpectsSave[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      mockKeystoreCache[Long](EstimateVatTurnoverKey.lastKnownValueKey, dummyCacheMap)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody("turnoverEstimate" -> "50000")) {
        _ redirectsTo s"$contextRoot/sell-zero-rated-items-next-12-months"
      }
    }

  }

}
