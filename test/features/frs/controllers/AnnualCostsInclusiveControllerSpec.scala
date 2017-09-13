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

package controllers.frs

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.frs.AnnualCostsInclusiveView
import models.view.vatFinancials.EstimateVatTurnover
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class AnnualCostsInclusiveControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val fakeRequest = FakeRequest(routes.AnnualCostsInclusiveController.show())

  object Controller
    extends AnnualCostsInclusiveController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  s"GET ${routes.AnnualCostsInclusiveController.show()}" should {

    "return HTML Annual Costs Inclusive page with no Selection" in {
      save4laterReturnsViewModel(AnnualCostsInclusiveView(""))()

      callAuthorised(Controller.show()) {
        _ includesText "Will the company spend more than £1,000 a year (including VAT) on business goods?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AnnualCostsInclusiveView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Will the company spend more than £1,000 a year (including VAT) on business goods?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[AnnualCostsInclusiveView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.show) {
        _ includesText "Will the company spend more than £1,000 a year (including VAT) on business goods?"
      }
    }
  }

  s"POST ${routes.AnnualCostsInclusiveController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with Annual Costs Inclusive selected Yes" in {
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES
      ))(_ redirectsTo s"$contextRoot/use-limited-cost-business-flat-rate")
    }

    "return 303 with Annual Costs Inclusive selected No - but within 12 months" in {
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS
      ))(_ redirectsTo s"$contextRoot/use-limited-cost-business-flat-rate")
    }

    "skip next question if 2% of estimated taxable turnover <= 1K and NO answered" in {
      save4laterExpectsSave[AnnualCostsInclusiveView]()
      save4laterReturnsViewModel(EstimateVatTurnover(25000L))()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      ))(_ redirectsTo s"$contextRoot/confirm-business-type")
    }

    "redirect to next question if 2% of estimated taxable turnover > 1K and NO answered" in {
      save4laterExpectsSave[AnnualCostsInclusiveView]()
      save4laterReturnsViewModel(EstimateVatTurnover(75000L))()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      ))(_ redirectsTo s"$contextRoot/spends-less-than-two-percent-of-turnover-a-year-on-goods")
    }
  }

}