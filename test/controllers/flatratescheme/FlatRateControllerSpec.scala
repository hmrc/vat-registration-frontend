/*
 * Copyright 2022 HM Revenue & Customs
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
import models.FlatRateScheme
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import views.html.flatratescheme._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FlatRateControllerSpec extends ControllerSpec with VatRegistrationFixture {

  val view = app.injector.instanceOf[frs_your_flat_rate]
  val annualCostsInclusiveView = app.injector.instanceOf[annual_costs_inclusive]
  val annualCostsLimitedView = app.injector.instanceOf[annual_costs_limited]
  val frsRegisterForView = app.injector.instanceOf[frs_register_for]
  val frsYourFlatRateView = app.injector.instanceOf[frs_your_flat_rate]

  trait Setup {
    val controller: FlatRateController = new FlatRateController(
      mockFlatRateService,
      mockVatRegistrationService,
      mockAuthClientConnector,
      mockSessionService,
      mockConfigConnector,
      mockTimeService,
      mockBusinessService,
      annualCostsInclusiveView,
      annualCostsLimitedView,
      frsRegisterForView,
      frsYourFlatRateView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${routes.FlatRateController.annualCostsInclusivePage}" should {
    "return a 200 when a previously completed S4LFlatRateScheme is returned" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.annualCostsInclusivePage) { result =>
        status(result) mustBe 200
      }
    }

    "return a 200 when an empty S4LFlatRateScheme is returned from the service" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(FlatRateScheme()))

      callAuthorised(controller.annualCostsInclusivePage) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualInclusiveCosts}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualInclusiveCosts)

    "return 400 with Empty data" in new Setup {
      val emptyRequest: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualInclusiveCosts, emptyRequest) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Annual Costs Inclusive selected Yes" in new Setup {
      when(mockFlatRateService.saveOverBusinessGoods(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {
      when(mockFlatRateService.saveOverBusinessGoods(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.FlatRateController.registerForFrsPage.url)
      }
    }
  }

  s"GET ${routes.FlatRateController.annualCostsLimitedPage}" should {
    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is not found on the vat scheme" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(overBusinessGoodsPercent = None, estimateTotalSales = Some(1234L))))

      callAuthorised(controller.annualCostsLimitedPage) { result =>
        status(result) mustBe 200
      }
    }

    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is found on the vat scheme" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      callAuthorised(controller.annualCostsLimitedPage) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualCostsLimited}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualCostsLimited)

    "return a 400 when the request is empty" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualCostsLimited, request) { result =>
        status(result) mustBe 400
      }
    }

    "redirect to confirm business sector when user selects Yes" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveOverBusinessGoodsPercent(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submitAnnualCostsLimited, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.ConfirmBusinessTypeController.show.url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveOverBusinessGoodsPercent(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      private val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submitAnnualCostsLimited, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.FlatRateController.registerForFrsPage.url)
      }
    }
  }

  s"GET ${routes.FlatRateController.registerForFrsPage}" should {
    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.registerForFrsPage) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.FlatRateController.submitRegisterForFrs}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitRegisterForFrs)

    "return 400 with Empty data" in new Setup {

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitRegisterForFrs, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {
      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submitRegisterForFrs, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.StartDateController.show.url)
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {
      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submitRegisterForFrs, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/attachments-resolve")
      }
    }
  }

  s"GET ${routes.FlatRateController.yourFlatRatePage}" should {
    "return a 200 and render the page" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(validFlatRate))
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      callAuthorised(controller.yourFlatRatePage) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.FlatRateController.submitYourFlatRate}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitYourFlatRate)

    "return 400 with Empty data" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitYourFlatRate, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Register For Flat Rate Scheme when Yes is selected" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveUseFlatRate(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submitYourFlatRate, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.StartDateController.show.url)
      }
    }

    "return 303 with Register For Flat Rate Scheme when No is selected" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveUseFlatRate(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submitYourFlatRate, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/attachments-resolve")
      }
    }
  }
}