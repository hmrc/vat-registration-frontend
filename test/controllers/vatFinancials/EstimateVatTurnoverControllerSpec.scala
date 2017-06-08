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
import models.view.vatFinancials.EstimateVatTurnover
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class EstimateVatTurnoverControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends EstimateVatTurnoverController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatFinancials.routes.EstimateVatTurnoverController.show())

  s"GET ${vatFinancials.routes.EstimateVatTurnoverController.show()}" should {

    "return HTML Estimate Vat Turnover page" in {
      save4laterReturns2(EstimateVatTurnover(100L))()

      callAuthorised(Controller.show()) {
        _ includesText "Estimated VAT taxable turnover for the next 12 months"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Estimated VAT taxable turnover for the next 12 months"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Estimated VAT taxable turnover for the next 12 months"
      }
    }
  }

  s"POST ${vatFinancials.routes.EstimateVatTurnoverController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }
  }

  s"POST ${vatFinancials.routes.EstimateVatTurnoverController.submit()} with a valid turnover estimate entered" should {

    "return 303" in {
      save4laterExpectsSave[EstimateVatTurnover]()
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody("turnoverEstimate" -> "50000")) {
        _ redirectsTo s"$contextRoot/sell-zero-rated-items-next-12-months"
      }
    }
  }

}
