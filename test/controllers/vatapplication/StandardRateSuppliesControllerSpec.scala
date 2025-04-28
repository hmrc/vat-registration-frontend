/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.vatapplication

import featuretoggle.FeatureSwitch.TaxableTurnoverJourney
import featuretoggle.FeatureToggleSupport
import fixtures.VatRegistrationFixture
import forms.{ReducedRateSuppliesForm, StandardRateSuppliesForm}
import models.api.vatapplication.VatApplication
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.vatapplication.StandardRateSupplies

import scala.concurrent.Future

class StandardRateSuppliesControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions with FeatureToggleSupport {

  class Setup {
    val standardRateSuppliesView: StandardRateSupplies = app.injector.instanceOf[StandardRateSupplies]
    val testController = new StandardRateSuppliesController(
      mockSessionService, mockAuthClientConnector, movkVatApplicationService, standardRateSuppliesView
    )
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
    val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val estimates: BigDecimal = BigDecimal(1000)
  val emptyReturns: VatApplication = VatApplication()
  val standardRatedSupplies: BigDecimal = BigDecimal(1000)
  val reducedRatedSupplies: BigDecimal = BigDecimal(500)

  "show with the Taxable Turnover feature flag disabled " should {
    "return OK if the user answered standardRatedSupplies question" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(standardRateSupplies = Some(standardRatedSupplies))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe standardRateSuppliesView(
          routes.StandardRateSuppliesController.submit,
          StandardRateSuppliesForm.form.fill(standardRatedSupplies)
        )(fakeRequest, messages, appConfig).toString()
      }
    }
  }


  "show with the New Journey feature flag enabled " should {
    "return OK if the user answered standardRatedSupplies and TurnOverEstimate question with the new journey view" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(standardRateSupplies = Some(standardRatedSupplies), turnoverEstimate = Some(estimates))))

      when(movkVatApplicationService.getReducedRated(any(), any(), any()))
        .thenReturn(Future.successful(Some(standardRatedSupplies)))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe standardRateSuppliesView(
          routes.StandardRateSuppliesController.submit,
          StandardRateSuppliesForm.form.fill(standardRatedSupplies)
        )(fakeRequest, messages, appConfig).toString()
      }
    }

    "return OK if the user answered TurnOverEstimate question with the new journey view" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(standardRateSupplies = None, turnoverEstimate = Some(estimates))))

      when(movkVatApplicationService.getReducedRated(any(), any(), any()))
        .thenReturn(Future.successful(Some(standardRatedSupplies)))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe standardRateSuppliesView(
          routes.StandardRateSuppliesController.submit,
          StandardRateSuppliesForm.form
        )(fakeRequest, messages, appConfig).toString()
      }
    }
  }


  "submit with the Taxable Turnover feature flag disabled" should {
    "redirect to the next page if everything is OK" in new Setup {
      disable(TaxableTurnoverJourney)

      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(standardRateSupplies = Some(standardRatedSupplies), turnoverEstimate = Some(estimates))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(standardRateSupplies = Some(standardRatedSupplies), turnoverEstimate = Some(estimates))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "standardRateSupplies" -> "1000"
      )
      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReducedRateSuppliesController.show.url)
      }
    }

    "return 400 for an invalid value" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getTurnover(any(), any(), any()))
        .thenReturn(Future.successful(Some(BigDecimal(1000))))

      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reducedRateSupplies = Some(standardRatedSupplies), turnoverEstimate = Some(estimates))))

      when(movkVatApplicationService.getReducedRated(any(), any(), any()))
        .thenReturn(Future.successful(Some(standardRatedSupplies)))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "standardRateSupplies" -> "invalid"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "submit with the Taxable Turnover feature flag enabled" should {
    val vatApplication: VatApplication = emptyReturns.copy(
      standardRateSupplies = Some(BigDecimal(1000)),
      zeroRatedSupplies = Some(BigDecimal(2000))
    )

    "redirect to the next page if everything is OK" in new Setup {
      enable(TaxableTurnoverJourney)

      when(mockSessionService.cache(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("test", Map())))

      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reducedRateSupplies = Some(reducedRatedSupplies),
          turnoverEstimate = Some(estimates), standardRateSupplies = Some(BigDecimal(1000)), zeroRatedSupplies = Some(BigDecimal(700)))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(reducedRateSupplies = Some(reducedRatedSupplies),
          turnoverEstimate = Some(estimates), standardRateSupplies = Some(BigDecimal(1000)), zeroRatedSupplies = Some(BigDecimal(700)))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "standardRateSupplies" -> "1000"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.ReducedRateSuppliesController.show.url)
      }
    }

    "return 400 for an invalid value" in new Setup {
      enable(TaxableTurnoverJourney)

      when(mockSessionService.cache(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("test", Map())))

      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(vatApplication))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "standardRateSupplies" -> "invalid"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }

}
