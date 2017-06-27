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
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class AnnualCostsInclusiveControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller
    extends AnnualCostsInclusiveController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.AnnualCostsInclusiveController.show())

  s"GET ${routes.AnnualCostsInclusiveController.show()}" should {

    "return HTML Annual Costs Inclusive page with no Selection" in {
      save4laterReturnsViewModel(AnnualCostsInclusiveView(""))()

      callAuthorised(Controller.show()) {
        _ includesText "Do you spend less than £1,000 a year (including VAT) on business goods?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AnnualCostsInclusiveView]()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "Do you spend less than £1,000 a year (including VAT) on business goods?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[AnnualCostsInclusiveView]()

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(Controller.show) {
        _ includesText "Do you spend less than £1,000 a year (including VAT) on business goods?"
      }
    }
  }

  s"POST ${routes.AnnualCostsInclusiveController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303 with Annual Costs Inclusive selected Yes" in {
      save4laterExpectsSave[AnnualCostsInclusiveView]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }

    "return 303 with Annual Costs Inclusive selected No - but within 12 months" in {
      save4laterExpectsSave[AnnualCostsInclusiveView]()

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS
      ))(_ redirectsTo s"$contextRoot/your-flat-rate")
    }

    "redirect to the welcome page with Annual Costs Inclusive selected No" in {
      when(mockS4LService.clear()(any())).thenReturn(Future.successful(validHttpResponse))
      save4laterExpectsSave[AnnualCostsInclusiveView]()
      when(mockVatRegistrationService.deleteVatScheme()(any())).thenReturn(Future.successful(()))

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> AnnualCostsInclusiveView.NO
      ))(_ redirectsTo s"$contextRoot/check-your-answers")
    }
  }

}
