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
import models.view.frs.AnnualCostsLimitedView
import models.view.vatFinancials.EstimateVatTurnover
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class AnnualCostsLimitedControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends  AnnualCostsLimitedController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AnnualCostsLimitedController.show())
  val estimateVatTurnover = EstimateVatTurnover(1000000L)
  s"GET ${routes.AnnualCostsLimitedController.show()}" should {

    "return HTML Annual Costs Limited page with no Selection" in {
      val annualCostsLimitedView = AnnualCostsLimitedView("")
      save4laterReturnsViewModel(annualCostsLimitedView)()

      save4laterReturnsViewModel(estimateVatTurnover)()

      callAuthorised(Controller.show()) {
        _ includesText "Do you spend less than £20,000"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AnnualCostsLimitedView]()
      save4laterReturnsViewModel(estimateVatTurnover)()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "Do you spend less than £20,000"
      }
    }

    "return HTML when there's not AnnualCostsLimitedView in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[AnnualCostsLimitedView]()
      save4laterReturnsViewModel(estimateVatTurnover)()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "Do you spend less than £20,000"
      }
    }

    "return HTML when there's both AnnualCostsLimitedView and EstimateVatTurnover in S4L and vatScheme no data" in {
      save4laterReturnsNoViewModel[AnnualCostsLimitedView]()
      save4laterReturnsNoViewModel[EstimateVatTurnover]()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "Do you spend less than £0"
      }
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit()} with Empty data" should {

    "return 400" in {
      save4laterReturnsViewModel(estimateVatTurnover)()
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit()} with Annual Costs Limited selected Yes" should {

    "return 303" in {
      save4laterExpectsSave[AnnualCostsLimitedView]()
      save4laterReturnsViewModel(estimateVatTurnover)()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit()} with Annual Costs Limited selected No - but within 12 months" should {

    "return 303" in {
      save4laterExpectsSave[AnnualCostsLimitedView]()
      save4laterReturnsViewModel(estimateVatTurnover)()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.YES_WITHIN_12_MONTHS
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit()} with Annual Costs Limited selected No" should {

    "redirect to the welcome page" in {
      when(mockS4LService.clear()(any())).thenReturn(Future.successful(validHttpResponse))
      save4laterExpectsSave[AnnualCostsLimitedView]()
      save4laterReturnsViewModel(estimateVatTurnover)()

      when(mockVatRegistrationService.deleteVatScheme()(any())).thenReturn(Future.successful(()))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.NO
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit()} with Annual Costs Limited selected No and EstimateVatTurnover is Null in S4l and Database" should {

    "redirect to the welcome page" in {
      when(mockS4LService.clear()(any())).thenReturn(Future.successful(validHttpResponse))
      save4laterExpectsSave[AnnualCostsLimitedView]()
      save4laterReturnsNoViewModel[EstimateVatTurnover]()
      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      when(mockVatRegistrationService.deleteVatScheme()(any())).thenReturn(Future.successful(()))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> AnnualCostsLimitedView.NO
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }
  }
}
