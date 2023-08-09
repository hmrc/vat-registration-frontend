/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.flatratescheme

import fixtures.VatRegistrationFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.FlatRateService.OverBusinessGoodsPercentAnswer
import testHelpers.ControllerSpec
import views.html.flatratescheme.AnnualCostsLimited

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnnualCostsLimitedControllerSpec extends ControllerSpec with VatRegistrationFixture {

  val view: AnnualCostsLimited = app.injector.instanceOf[AnnualCostsLimited]

  trait Setup {
    val controller: AnnualCostsLimitedController = new AnnualCostsLimitedController(
      mockFlatRateService,
      mockAuthClientConnector,
      mockSessionService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }



  s"GET ${routes.AnnualCostsLimitedController.show}" should {
    "return a 200 and render Annual Costs Limited page when a FlatRateScheme is not found on the vat scheme" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(overBusinessGoodsPercent = None, estimateTotalSales = Some(1234L))))
      when(mockFlatRateService.applyPercentRoundUp(any())).thenReturn(BigDecimal(0))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }

    "return a 200 and render Annual Costs Limited page when a FlatRateScheme is found on the vat scheme" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))
      when(mockFlatRateService.applyPercentRoundUp(any())).thenReturn(BigDecimal(0))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.AnnualCostsLimitedController.submit}" should {
    val fakeRequest = FakeRequest(routes.AnnualCostsLimitedController.submit)

    "return a 400 when the request is empty" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))
      when(mockFlatRateService.applyPercentRoundUp(any())).thenReturn(BigDecimal(0))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody()

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "redirect to confirm business sector when user selects Yes" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveFlatRate(any[OverBusinessGoodsPercentAnswer]())(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.ConfirmBusinessTypeController.show.url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveFlatRate(any[OverBusinessGoodsPercentAnswer]())(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      private val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.RegisterForFrsController.show.url)
      }
    }
  }

}
