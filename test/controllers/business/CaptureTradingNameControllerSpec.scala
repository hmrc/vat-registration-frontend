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

package controllers.business

import fixtures.VatRegistrationFixture
import models.CurrentProfile
import models.api.UkCompany
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.business.CaptureTradingNameView

import scala.concurrent.Future

class CaptureTradingNameControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val fakeRequest = FakeRequest(controllers.business.routes.CaptureTradingNameController.show)

  class Setup {
    val view = app.injector.instanceOf[CaptureTradingNameView]
    val testController = new CaptureTradingNameController(
      mockSessionService,
      mockAuthClientConnector,
      mockApplicantDetailsServiceOld,
      mockBusinessService,
      mockVatRegistrationService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" must {
    "return an Ok when there is a trading name answer" in new Setup {
      mockGetBusiness(Future.successful(validBusiness.copy(tradingName = Some(testTradingName))))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "return an Ok when there is no trading name answer" in new Setup {
      mockGetBusiness(Future.successful(validBusiness.copy(tradingName = None)))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }
  }

  "submit" must {
    "return 303 with a provided trading name" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness.copy(tradingName = Some(testTradingName))))
      when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier]))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "captureTradingName" -> testTradingName
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.business.routes.PpobAddressController.startJourney.url)
      }
    }

    "return 400 without a provided trading name" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "captureTradingName" -> ""
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when trading name is empty" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody()

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when the trading name they have provided is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "captureTradingName" -> "$0M3 T3$T"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }
  }

}
