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

package controllers.flatratescheme

import fixtures.VatRegistrationFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.FlatRateService.UseThisRateAnswer
import testHelpers.ControllerSpec
import views.html.flatratescheme._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class YourFlatRateControllerSpec extends ControllerSpec with VatRegistrationFixture {

  val view: YourFlatRate = app.injector.instanceOf[YourFlatRate]

  trait Setup {
    val controller: YourFlatRateController = new YourFlatRateController(
      mockFlatRateService,
      mockAuthClientConnector,
      mockSessionService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${routes.YourFlatRateController.show}" should {
    "return a 200 and render the page" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))
      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      callAuthorised(controller.show) { result =>
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.YourFlatRateController.submit}" should {
    val fakeRequest = FakeRequest(routes.YourFlatRateController.submit)

    "return 400 with Empty data" in new Setup {
      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody()

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Register For Flat Rate Scheme when Yes is selected" in new Setup {
      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      when(mockFlatRateService.saveFlatRate(any[UseThisRateAnswer]())(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.flatratescheme.routes.StartDateController.show.url)
      }
    }

    "return 303 with Register For Flat Rate Scheme when No is selected" in new Setup {
      when(mockFlatRateService.retrieveBusinessTypeDetails(any(), any(), any()))
        .thenReturn(Future.successful(testBusinessTypeDetails))

      when(mockFlatRateService.saveFlatRate(any[UseThisRateAnswer]())(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(controller.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }
}