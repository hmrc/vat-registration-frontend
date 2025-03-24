/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.ZeroRatedSuppliesForm
import models.api.vatapplication.VatApplication
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.vatapplication.{ZeroRatedSupplies, ZeroRatedSuppliesNewJourney}

import scala.concurrent.Future

class ZeroRatedSuppliesControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions with FeatureToggleSupport {

  class Setup {
    val zeroRatedSuppliesView: ZeroRatedSupplies = app.injector.instanceOf[ZeroRatedSupplies]
    val zeroRatedSuppliesNewJourneyView: ZeroRatedSuppliesNewJourney = app.injector.instanceOf[ZeroRatedSuppliesNewJourney]
    val testController = new ZeroRatedSuppliesController(
      mockSessionService, mockAuthClientConnector, movkVatApplicationService, zeroRatedSuppliesView, zeroRatedSuppliesNewJourneyView
    )
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
    val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

  }

  val estimates: BigDecimal = BigDecimal(1000)
  val zeroRatedSupplies: BigDecimal = BigDecimal(1000)
  val emptyReturns: VatApplication = VatApplication()

  "show with the New Journey feature flag disabled " should {
    "return OK if the user answered ZeroRatedSupply and TurnOverEstimate question" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = Some(zeroRatedSupplies), turnoverEstimate = Some(estimates))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe zeroRatedSuppliesView(
          routes.ZeroRatedSuppliesController.submit,
          ZeroRatedSuppliesForm.form(estimates).fill(zeroRatedSupplies)
        )(fakeRequest, messages, appConfig).toString()
      }
    }

    "return OK if the has answered TurnOverEstimate question" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = None, turnoverEstimate = Some(estimates))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe zeroRatedSuppliesView(
          routes.ZeroRatedSuppliesController.submit,
          ZeroRatedSuppliesForm.form(estimates)
        )(fakeRequest, messages, appConfig).toString()
      }
    }

    "return Missing Exception if the user has not answered" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = None, turnoverEstimate = None)))
      when(mockSessionService.cache(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("test", Map())))

      callAuthorised(testController.show) { result =>
        status(result) mustBe SEE_OTHER
      }
    }

  }
  "show with the New Journey feature flag enabled " should {
    "return OK if the user answered ZeroRatedSupply and TurnOverEstimate question with the new journey view if the feature flag is enabled" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = Some(zeroRatedSupplies), turnoverEstimate = Some(estimates))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe zeroRatedSuppliesNewJourneyView(
          routes.ZeroRatedSuppliesController.submit,
          ZeroRatedSuppliesForm.form(estimates).fill(zeroRatedSupplies)
        )(fakeRequest, messages, appConfig).toString()
      }
    }

    "return OK if the user answered TurnOverEstimate question with the new journey view if the feature flag is enabled" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = None, turnoverEstimate = Some(estimates))))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentAsString(result) mustBe zeroRatedSuppliesNewJourneyView(
          routes.ZeroRatedSuppliesController.submit,
          ZeroRatedSuppliesForm.form(estimates)
        )(fakeRequest, messages, appConfig).toString()
      }
    }

    "return Missing Exception if the user has not answered" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getVatApplication(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns.copy(zeroRatedSupplies = None, turnoverEstimate = None)))
      when(mockSessionService.cache(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("test", Map())))

      callAuthorised(testController.show) { result =>
        status(result) mustBe SEE_OTHER
      }
    }
  }

  "submit with the New Journey feature flag disabled" should {
    "redirect to the next page if everything is OK" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getTurnover(any(), any(), any()))
        .thenReturn(Future.successful(Some(BigDecimal(1000))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "zeroRatedSupplies" -> "1000"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.SellOrMoveNipController.show.url)
      }
    }

    "return 400 for an invalid value" in new Setup {
      disable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getTurnover(any(), any(), any()))
        .thenReturn(Future.successful(Some(BigDecimal(1000))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "zeroRatedSupplies" -> "invalid"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "submit with the New Journey feature flag enabled" should {
    "redirect to the next page if everything is OK" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getTurnover(any(), any(), any()))
        .thenReturn(Future.successful(Some(BigDecimal(1000))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "zeroRatedSupplies" -> "1000"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.vatapplication.routes.SellOrMoveNipController.show.url)
      }
    }

    "return 400 for an invalid value" in new Setup {
      enable(TaxableTurnoverJourney)
      when(movkVatApplicationService.getTurnover(any(), any(), any()))
        .thenReturn(Future.successful(Some(BigDecimal(1000))))

      when(movkVatApplicationService.saveVatApplication(any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyReturns))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "zeroRatedSupplies" -> "invalid"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

  }
}
